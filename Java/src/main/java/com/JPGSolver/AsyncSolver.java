package com.JPGSolver;

import com.JPGSolver.Graph.Node;

import com.google.common.primitives.Ints;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;


public class AsyncSolver implements Solver {

    protected Graph G;
    protected int[] tmpMap;
    protected int[] check;
    protected ExecutorService executor;

    public AsyncSolver(){
        int processors = Runtime.getRuntime().availableProcessors();
        //processors = 1;
        System.out.println("Processors: " + processors);
        executor = Executors.newFixedThreadPool(processors);
    }

    @Override
    public int[][] win(Graph G) {
        this.G = G;
        this.tmpMap = new int[G.length()];
        this.check = new int[G.length()];
        BitSet removed = new BitSet(G.length());
        int[][] res =  win_improved(removed);
        //int[][] res =  pre_improved(removed);
        //int[][] res = win_banal();
        //int[][] res = win_banal_async();
        executor.shutdown();
        return res;
    }


    public static boolean checkBanal(Node node, Node next){
        int nodeP = node.getPlayer();
        return nodeP == next.getPlayer() && Integer.max(node.getPriority(), next.getPriority()) % 2 == nodeP && next.getAdj().contains(node.getIndex());
    }

    protected class asyncBanal implements Callable<TIntArrayList[]> {

        int nodeI;

        public asyncBanal(int node){
            this.nodeI = node;
        }

        private boolean maxPP(Node n1, Node n2, int player){
            int priority = Integer.max(n1.getPriority(), n2.getPriority());
            if (player == 0) return priority % 2 == 0;
            else  return priority % 2 != 0;
        }

        public TIntArrayList[] call() {
            if (tmpMap[nodeI] == 1)
                return null;
            TIntArrayList[] banal = new TIntArrayList[2];
            banal[0] = new TIntArrayList();
            banal[1] = new TIntArrayList();

            Node node = G.info[nodeI];
            int nodeP = node.getPlayer();
            TIntIterator nextIt = node.getAdj().iterator();
            while (nextIt.hasNext()){
                int nextI = nextIt.next();
                Node next = G.info[nextI];
                int nextP = next.getPlayer();
                if (checkBanal(node, next)){
                    if (tmpMap[nodeI] != 1){
                        tmpMap[nodeI] = 1;
                        banal[nodeP].add(nodeI);
                    }
                    if (tmpMap[nextI] != 1){
                        tmpMap[nextI] = 1;
                        banal[nextP].add(nextI);
                    }
                }
            }

            return banal;
        }
    }

    public int[][] win_banal_async(){
        TIntArrayList[] banal = new TIntArrayList[2];
        banal[0] = new TIntArrayList();
        banal[1] = new TIntArrayList();

        ArrayList<FutureTask<TIntArrayList[]>> tasks = new ArrayList<>();

        for (int node = 0; node < G.length(); node++){
            FutureTask<TIntArrayList[]> task = new FutureTask<>(new asyncBanal(node));
            tasks.add(task);
            executor.execute(task);
        }

        try {
            //for (FutureTask<TIntArrayList[]> task : tasks) executor.execute(task);
            for (FutureTask<TIntArrayList[]> task : tasks){
                TIntArrayList[] get = task.get();
                if (get == null) continue;
                MyTrove.addAllEx(banal[0], get[0], check);
                MyTrove.addAllEx(banal[1], get[1], check);
            }
            tasks.clear();
        } catch (Exception e) {
            executor.shutdown();
            throw new RuntimeException("Future get Exception");
        }

        System.out.println("Banals: " + (banal[0].size() + banal[1].size()));
        int[][] W = {banal[0].toArray(), banal[1].toArray()};
        return W;
    }

    public int[][] win_banal(){
        TIntArrayList[] banal = new TIntArrayList[2];
        banal[0] = new TIntArrayList();
        banal[1] = new TIntArrayList();

        for (int nodeI = 0; nodeI < G.length(); nodeI++){
            if (tmpMap[nodeI] == 1)
                continue;
            Node node = G.info[nodeI];
            int nodeP = node.getPlayer();
            TIntIterator nextIt = node.getAdj().iterator();
            while (nextIt.hasNext()){
                int nextI = nextIt.next();
                Node next = G.info[nextI];
                int nextP = next.getPlayer();
                if (checkBanal(node, next)){
                    if (tmpMap[nodeI] != 1){
                        tmpMap[nodeI] = 1;
                        banal[nodeP].add(nodeI);
                    }
                    if (tmpMap[nextI] != 1){
                        tmpMap[nextI] = 1;
                        banal[nextP].add(nextI);
                    }
                }
            }
        }
        System.out.println("Banals: " + (banal[0].size() + banal[1].size()));
        int[][] W = {banal[0].toArray(), banal[1].toArray()};
        return W;
    }

    protected class asyncAttr implements Callable<TIntArrayList> {

        int node;
        BitSet removed;
        int i;

        public asyncAttr(int node, BitSet removed, int i){
            this.node = node;
            this.removed = removed;
            this.i = i;
        }

        public TIntArrayList call() {
            final TIntIterator iter = G.incomingEdgesOf(node).iterator();
            TIntArrayList A = new TIntArrayList();
            while (iter.hasNext()) {
                int v0 = iter.next();
                if (!removed.get(v0)) {
                    boolean flag = G.getPlayerOf(v0) == i;
                    synchronized (G.info[v0]) {
                        if (tmpMap[v0] == 0) {
                            if (flag) {
                                A.add(v0);
                                tmpMap[v0] = 1;
                            } else {
                                int adjCounter = 0;
                                TIntIterator it = G.outgoingEdgesOf(v0).iterator();
                                while (it.hasNext()) {
                                    if (!removed.get(it.next())) {
                                        adjCounter += 1;
                                    }
                                }
//                                try {
//                                    Thread.sleep(5);
//                                } catch (InterruptedException ex) {
//                                    Thread.currentThread().interrupt();
//                                }
                                tmpMap[v0] = adjCounter;
                                if (adjCounter == 1) {
                                    A.add(v0);
                                }
                            }
                        } else if (!flag && tmpMap[v0] > 1) {
                            tmpMap[v0] -= 1;
                            if (tmpMap[v0] == 1) {
                                A.add(v0);
                            }
                        }
                    }

                }
            }
            return A;
        }
    }

    protected TIntArrayList Attr(TIntArrayList A, int i, BitSet removed) {
        Arrays.parallelSetAll(tmpMap, x -> 0);
        Arrays.parallelSetAll(check, x -> 0);
        TIntIterator it = A.iterator();
        while (it.hasNext()) {
            tmpMap[it.next()] = 1;
        }

        int index = 0;
        ArrayList<FutureTask<TIntArrayList>> tasks = new ArrayList<>();
        while (index < A.size()) {
            while (index < A.size()) {
                tasks.add(new FutureTask<>(new asyncAttr(A.get(index), removed, i)));
                index += 1;
            }

            try {
                for (FutureTask<TIntArrayList> task : tasks) executor.execute(task);
                for (FutureTask<TIntArrayList> task : tasks) MyTrove.addAllEx(A, task.get(), check);
                tasks.clear();
            } catch (Exception e) {
                executor.shutdown();
                throw new RuntimeException("Future get Exception");
            }
        }
        it = A.iterator();
        while (it.hasNext()) {
            removed.set(it.next());
        }
        return A;
    }

    private int[][] pre_improved(BitSet removed) {
        final int[][] W = {new int[0], new int[0]};
        final int d = G.maxPriority(removed);
        if (d > -1) {
            TIntArrayList U = G.getNodesWithPriority(d, removed);
            final int p = d % 2;
            final int j = 1 - p;
            int[][] W1;
            W1 = win_banal();
            if (W1[j].length == 0) {
                W[p] = W1[p];
            } else {
                BitSet removed2 = (BitSet)removed.clone();
                final TIntArrayList B = Attr(new TIntArrayList(W1[j]), j, removed2);
                W1 = win_improved(removed2);
                W[p] = W1[p];
                W[j] = Ints.concat(W1[j], B.toArray());
            }
        }
        return W;
    }

    private int[][] win_improved(BitSet removed) {
        final int[][] W = {new int[0], new int[0]};
        final int d = G.maxPriority(removed);
        if (d > -1) {
            TIntArrayList U = G.getNodesWithPriority(d, removed);
            final int p = d % 2;
            final int j = 1 - p;
            int[][] W1;
            BitSet removed1 = (BitSet)removed.clone();
            final TIntArrayList A = Attr(U, p, removed1);
            W1 = win_improved(removed1);
            if (W1[j].length == 0) {
                W[p] = Ints.concat(W1[p], A.toArray());
            } else {
                BitSet removed2 = (BitSet)removed.clone();
                final TIntArrayList B = Attr(new TIntArrayList(W1[j]), j, removed2);
                W1 = win_improved(removed2);
                W[p] = W1[p];
                W[j] = Ints.concat(W1[j], B.toArray());
            }
        }
        return W;
    }
}
