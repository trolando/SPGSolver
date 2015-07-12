package com.JPGSolver;
import gnu.trove.list.array.TIntArrayList;
import java.io.*;
import java.util.BitSet;
import java.util.Optional;
import java.util.stream.Stream;

public class Graph {
    public static class Node {
        private int index = -1;
        private int player = -1;
        private int priority = -1;
        private final TIntArrayList adj = new TIntArrayList();
        private final TIntArrayList inj = new TIntArrayList();


        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public int getPlayer() {
            return player;
        }

        public void setPlayer(int player) {
            this.player = player;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public void addAdj(int destination) {
            adj.add(destination);
        }

        public void addInj(int origin) {
            synchronized (this.inj) {
                inj.add(origin);
            }
        }

        public TIntArrayList getAdj() {
            return adj;
        }

        public TIntArrayList getInj() {
            return inj;
        }
    }

    private final Node[] info;

    public Graph(int numNodes) {
        info = new Node[numNodes];
        for (int i = 0; i < numNodes; i++) {
            info[i] = new Node();
        }
    }

    public int length() {
        return info.length;
    }

    public int getPlayerOf(final int v) {
        return info[v].getPlayer();
    }

    public void addEdge(final int origin, final int destination) {
        info[origin].addAdj(destination);
        info[destination].addInj(origin);
    }

    public int maxPriority(BitSet removed) {
        Optional<Node> maxNode = Stream.of(info)
                .filter(x -> !removed.get(x.getIndex()))
                .max((x, y) -> Integer.compare(x.getPriority(), y.getPriority()));
        return maxNode.isPresent() ? maxNode.get().getPriority() : -1;
    }

    public TIntArrayList getNodesWithPriority(final int priority, BitSet removed) {
        final TIntArrayList res = new TIntArrayList();
        Stream.of(info)
                .parallel()
                .filter(x -> !removed.get(x.getIndex()) && x.getPriority() == priority)
                .forEach(x -> res.add(x.getIndex()));
        return res;
    }

    public TIntArrayList incomingEdgesOf(final int v) {
        return info[v].getInj();
    }

    public TIntArrayList outgoingEdgesOf(final int v) {
        return info[v].getAdj();
    }

    public static Graph initFromFile(String file) {
        System.out.println("Parsing Graph from .............. " + file);
        Graph graph = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            Optional<String> first = br.lines().findFirst();
            if (first.isPresent()) {
                String[] ln = first.get().split(" ");
                graph = new Graph(Integer.parseInt(ln[1].substring(0, ln[1].length() - 1)) + 1);
            } else {
                throw new RuntimeException("Invalid file passed as arena.");
            }
            final Graph G = graph;
            br.lines().parallel().forEach(line -> {
                String[] x = line.split(" ");
                String[] edges = x[3].split(",");
                int node = Integer.parseInt(x[0]);
                G.info[node].setIndex(node);
                G.info[node].setPriority(Integer.parseInt(x[1]));
                G.info[node].setPlayer(Integer.parseInt(x[2]));
                for (String edge : edges) {
                    if (edge.endsWith(";")) {
                        G.addEdge(node, Integer.parseInt(edge.substring(0, edge.length() - 1)));
                    } else {
                        G.addEdge(node, Integer.parseInt(edge));
                    }
                }
            });
        } catch (FileNotFoundException e) {
            System.out.println("File not found, please check your input.");
        }
        return graph;
    }
}