//
//  CPPSolver.cpp
//  C++ Parity Games Solver
//

#include <array>
#include <chrono>
#include <fstream>
#include <future>
#include <iostream>
#include <map>
#include <vector>
#include <sstream>
#include <boost/algorithm/string/classification.hpp>
#include <boost/algorithm/string/split.hpp>
#include <boost/program_options.hpp>
#include <boost/dynamic_bitset.hpp>

class Node {
private:
    std::vector<int> adj;
    std::vector<int> inj;
    int priority;
    int player;
public:
    std::mutex mutex;
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
async_attr(Graph* G, std::vector<int>* tmpMap, int inode, std::vector<bool>* removed, int i){
    std::vector<int> A;
    Node* node = &G->get(inode);
    for (const int v0 : node->get_inj()) {
        if (!(*removed)[v0]) {
            G->get(v0).mutex.lock();
            auto flag = G->get(v0).get_player() == i;
            if ((*tmpMap)[v0] == -1) {
                if (flag) {
                    A.push_back(v0);
                    (*tmpMap)[v0] = 0;
                } else {
                    int adj_counter = -1;
                    for (const int x : G->get(v0).get_adj()) {
                        if (!(*removed)[x]) {
                            adj_counter += 1;
                        }
                    }
                    (*tmpMap)[v0] += adj_counter;
                    if (adj_counter == 0) {
                        A.push_back(v0);
                    }
                }
            } else if (!flag and (*tmpMap)[v0] > 0) {
                (*tmpMap)[v0] -= 1;
                if ((*tmpMap)[v0] == 0) {
                    A.push_back(v0);
                }
            }
            G->get(v0).mutex.unlock();
        }
    }
    return A;
}

std::vector<int>
concurrent_attr(Graph& G, std::vector<bool>& removed, std::vector<int>& A, int i) {
    std::vector<int> tmpMap(G.size(), -1);
    std::vector<bool> check(G.size());
    for (const int x : A) {
        tmpMap[x] = 0;
        check[x] = true;
    }
    
    int index = 0;
    std::vector<std::future<std::vector<int>>> results;
    while (index < A.size()) {
        while (index < A.size()) {
            results.push_back(std::async(async_attr, &G, &tmpMap, A[index], &removed, i));
            index += 1;
        }
        for (int i = 0; i < results.size(); i++) {
            auto res = results[i].get();
            for (auto i : res) {
                if (!check[i]) {
                    check[i] = true;
                    A.push_back(i);
                }
            }
        }
        results.clear();
    }
    
    return A;
}

std::vector<int>
attr(Graph& G, std::vector<bool>& removed, std::vector<int>& A, int i) {
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
win_concurrent(Graph& G, std::vector<bool>& removed) {
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
        auto A = concurrent_attr(G, removed, U, p);
        std::vector<bool> removed1(removed);
        for (const int x : A) {
            removed1[x] = true;
        }
        W1 = win_concurrent(G, removed1);
        if (W1[j].size() == 0) {
            std::merge(W1[p].begin(), W1[p].end(), A.begin(), A.end(),
                       std::back_inserter(W[p]));
        } else {
            auto B = concurrent_attr(G, removed, W1[j], j);
            std::vector<bool> removed2(removed);
            for (const int x : B) {
                removed2[x] = true;
            }
            W1 = win_concurrent(G, removed2);
            W[p] = W1[p];
            std::merge(W1[j].begin(), W1[j].end(), B.begin(), B.end(),
                       std::back_inserter(W[j]));
        }
    }
    return W;
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
        auto A = attr(G, removed, U, p);
        std::vector<bool> removed1(removed);
        for (const int x : A) {
            removed1[x] = true;
        }
        W1 = win_improved(G, removed1);
        if (W1[j].size() == 0) {
            std::merge(W1[p].begin(), W1[p].end(), A.begin(), A.end(),
                       std::back_inserter(W[p]));
        } else {
            auto B = attr(G, removed, W1[j], j);
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
win(Graph& G, std::function<std::array<std::vector<int>, 2>(Graph& G, std::vector<bool>&)> f) {
    std::chrono::time_point<std::chrono::system_clock> start, end;
    start = std::chrono::system_clock::now();
    auto removed = std::vector<bool>(G.size(), false);
    auto res = f(G, removed);
    end = std::chrono::system_clock::now();
    std::chrono::duration<double> elapsed_seconds = end-start;
    printf("Solved in ........... %fs \n", elapsed_seconds.count());
    return res;
}

Graph*
init_graph_from_file(std::string argf) {
    // record current time
    std::chrono::time_point<std::chrono::system_clock> start, end;
    start = std::chrono::system_clock::now();
    // get ifstream for file
    std::ifstream ifs(argf);
    // read header line...
    std::string line;
    int numNodes = 0;
    while (getline(ifs, line)) {
        std::stringstream ss(line);
        std::string token;
        if (!(ss>>token)) continue; // ignore empty lines
        if (token != "parity") throw "Invalid file, expecting parity header!";
        if (!(ss >> numNodes)) throw "Invalid file, missing number of nodes!";
        break;
    }
    Graph *G = new Graph(numNodes + 1);
    // read spec...
    while (getline(ifs, line)) {
        std::stringstream ss(line);
        std::string token;
        int node, priority, owner;
        try {
            if (!(ss>>node)) continue; // ignore empty line
        } catch (const std::invalid_argument) {
            continue; // ignore lines starting with a non number
        }
        if (!(ss >> priority)) throw "missing priority";
        if (!(ss >> owner)) throw "missing owner";
        G->addNode(node, priority, owner);
        for (;;) {
            int to;
            if (!(ss >> to)) throw "missing successor";
            G->addEdge(node, to);
            char ch;
            if (!(ss >> ch)) throw "missing ; to end line";
            if (ch == ',') continue; // next successor
            break; // no next successor, ignore rest
        }
    }
    // record time again and report how long it took
    end = std::chrono::system_clock::now();
    std::chrono::duration<double> elapsed_seconds = end-start;
    std::cout << "Parsed in ........... " << elapsed_seconds.count() << "s" << std::endl;
    return G;
}

int
main(int argc, const char * argv[]) {
    namespace po = boost::program_options;
    po::options_description desc("Options");
    desc.add_options()
    ("help,h", "Help Screen")
    ("concurrent", po::value<bool>()->default_value(false), "Use concurrent solver")
    ("files", po::value<std::vector<std::string>>(), "Input Files")
    ("justHeat", po::value<bool>()->default_value(false), "Prevent to print solutions");
    
    po::variables_map variables_map;
    po::positional_options_description files;
    files.add("files", 1);
    
    try {
        po::store(po::command_line_parser(argc, argv).options(desc).positional(files).run(), variables_map);
        po::notify(variables_map);
    } catch (po::error& e) {
        std::cerr << std::endl << e.what() << std::endl;
        return 1;
    }
    
    if (!variables_map.count("files")) {
        std::cerr << "No file specified. Aborting." << std::endl;
        return 1;
    } else if (variables_map.count("files") > 1) {
        std::cerr << "WARN: Multiple files found, solving the first one only." << std::endl;
    }
    
    auto file_paths = variables_map["files"].as<std::vector<std::string>>();
    
    std::cout << "Parsing from ........ " << file_paths[0] << std::endl;
    auto G = init_graph_from_file(file_paths[0]);
    
    std::array<std::vector<int>, 2> solutions;
    
    if (variables_map["concurrent"].as<bool>()) {
        solutions = win(*G, win_concurrent);
    } else {
        solutions = win(*G, win_improved);
    }
    
    
    
    if (variables_map["justHeat"].as<bool>()) {
        return 0;
    }
    
    std::cout << "\nSolution for Player 0:" << std::endl;
    std::sort(solutions[0].begin(), solutions[0].end());
    std::sort(solutions[1].begin(), solutions[1].end());
    std::cout << "{";
    for (auto &v : solutions[0]) {
        if (v == solutions[0][solutions[0].size()-1]) {
            printf("%d}", v);
        } else {
            printf("%d, ", v);
        }
    }
    printf("\n\nSolution for Player 1:\n{");
    for (auto &v : solutions[1]) {
        if (v == solutions[1][solutions[1].size()-1]) {
            printf("%d}", v);
        } else {
            printf("%d, ", v);
        }
    }
    printf("\n");
    delete G;
    return 0;
}
