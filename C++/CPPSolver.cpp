//
//  CPPSolver.cpp
//  C++ Parity Games Solver
//
//  Copyright (c) 2014 Vincenzo Prignano. All rights reserved.
//

#include <iostream>
#include <map>
#include <vector>
#include <array>
#include <fstream>
#include <string>
#include <memory>
#include <boost/algorithm/string/split.hpp>
#include <boost/algorithm/string/classification.hpp>
#include <boost/timer.hpp>


class Node {
private:
    std::vector<int> adj;
    std::vector<int> inj;
    int priority;
    int player;
public:
    Node() {
        priority = -1;
        player = -1;
    }
    void set_priority(int pr) {
        priority = pr;
    }
    void set_player(int pl) {
        player = pl;
    }
    int const get_priority() {
        return priority;
    }
    int const get_player() {
        return player;
    }
    std::vector<int> const &get_adj() {
        return adj;
    }
    std::vector<int> const &get_inj() {
        return inj;
    }
    void add_adj(int other) {
        adj.push_back(other);
    }
    void add_inj(int other) {
        inj.push_back(other);
    }
};

class Graph {
private:
    std::vector<Node> nodes;
    std::map<int, std::vector<int>> priorityMap;
public:
    Graph(int numNodes) {
        nodes = std::vector<Node>(numNodes);
    }
    Node &get(long n) {
        return nodes[n];
    }
    void addNode(int node, int priority, int player) {
        priorityMap[priority].push_back(node);
        nodes[node].set_priority(priority);
        nodes[node].set_player(player);
    }
    void addEdge(int origin, int destination) {
        nodes[origin].add_adj(destination);
        nodes[destination].add_inj(origin);
    }
    long size() {
        return nodes.size();
    }
    std::map<int, std::vector<int>> &get_priority_map() {
        return priorityMap;
    }
};


int
max_priority(Graph& G, std::vector<bool>& removed) {
    int max = -1;
    for (long v = 0; v < G.size(); v++) {
        if (!removed[v] && G.get(v).get_priority() > max) {
            max = G.get(v).get_priority();
        }
    }
    return max;
}

std::vector<int>
Attr(Graph& G, std::vector<bool>& removed, std::vector<int>& A, int i) {
    std::vector<int> tmpMap(G.size(), -1);
    for (const int x : A) {
        tmpMap[x] = 0;
    }
    auto index = 0;
    while (index < A.size()) {
        for (const int v0 : G.get(A[index]).get_inj()) {
            if (!removed[v0]) {
                auto flag = G.get(v0).get_player() == i;
                if (tmpMap[v0] == -1) {
                    if (flag) {
                        A.push_back(v0);
                        tmpMap[v0] = 0;
                    } else {
                        int adj_counter = -1;
                        for (const int x : G.get(v0).get_adj()) {
                            if (!removed[x]) {
                                adj_counter += 1;
                            }
                        }
                        tmpMap[v0] = adj_counter;
                        if (adj_counter == 0) {
                            A.push_back(v0);
                        }
                    }
                } else if (!flag and tmpMap[v0] > 0) {
                    tmpMap[v0] -= 1;
                    if (tmpMap[v0] == 0) {
                        A.push_back(v0);
                    }
                }
            }
        }
        index += 1;
    }
    return A;
}

std::array<std::vector<int>, 2>
win_improved(Graph& G, std::vector<bool>& removed) {
    std::array<std::vector<int>, 2> W;
    auto d = max_priority(G, removed);
    if (d > -1) {
        std::vector<int> U;
        for (const int x : G.get_priority_map()[d]) {
            if (!removed[x]) {
                U.push_back(x);
            }
        }
        int p = d % 2;
        int j = 1 - p;
        std::array<std::vector<int>, 2> W1;
        auto A = Attr(G, removed, U, p);
        std::vector<bool> removed1(removed);
        for (const int x : A) {
            removed1[x] = true;
        }
        W1 = win_improved(G, removed1);
        if (W1[j].size() == 0) {
            std::merge(W1[p].begin(), W1[p].end(), A.begin(), A.end(),
                           std::back_inserter(W[p]));
        } else {
            auto B = Attr(G, removed, W1[j], j);
            std::vector<bool> removed2(removed);
            for (const int x : B) {
                removed2[x] = true;
            }
            W1 = win_improved(G, removed2);
            W[p] = W1[p];
            std::merge(W1[j].begin(), W1[j].end(), B.begin(), B.end(),
                           std::back_inserter(W[j]));
        }
    }
    return W;
}

std::array<std::vector<int>, 2>
win(Graph& G) {
    auto removed = std::vector<bool>(G.size(), false);
    boost::timer t;
    auto res = win_improved(G, removed);
    printf("Solved in ........... %f \n", t.elapsed());
    return res;
}

Graph
initGraph(std::string argf) {
    boost::timer t;
    std::ifstream ifs(argf);
    std::string line;
    std::string first;
    std::getline(ifs, first);
    int numNodes = 0;
    if (first.compare("parity") > -1) {
        std::vector<std::string> y;
        boost::split(y, first, boost::is_any_of(" "));
        numNodes = atoi(y[1].substr(0, y[1].size()-1).c_str());
    } else {
        throw "Invalid file Passed as argument.";
    }
    Graph G(numNodes + 1);
    while (std::getline(ifs, line)) {
        std::vector<std::string> x, edges;
        boost::split(x, line, boost::is_any_of(" "));
        int node = std::atoi(x[0].c_str());
        G.addNode(node, std::atoi(x[1].c_str()), std::atoi(x[2].c_str()));
        boost::split(edges, x[3], boost::is_any_of(","));
        for (const auto& x : edges) {
            G.addEdge(node, atoi(x.c_str()));
        }
    }
    ifs.close();
    std::cout << "Parsed in ........... " << t.elapsed() << std::endl;
    return G;
}

int
main(int argc, const char * argv[]) {
    std::cout << "Parsing from ........ " << argv[1] << std::endl;
    auto G = initGraph(argv[1]);
    auto solution = win(G);
printf("\nSolution for Player 0: \n");
std::sort(solution[0].begin(), solution[0].end());
std::sort(solution[1].begin(), solution[1].end());
printf("{");
for (auto &v : solution[0]) {
    if (v == solution[0][solution[0].size()-1]) {
        printf("%d}", v);
    } else {
        printf("%d, ", v);
    }
}
printf("\n\nSolution for Player 1:\n{");
for (auto &v : solution[1]) {
        if (v == solution[1][solution[1].size()-1]) {
            printf("%d}", v);
        } else {
            printf("%d, ", v);
        }
    }
    printf("\n");
    return 0;
}
