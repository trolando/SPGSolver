package com.JPGSolver;

import com.google.common.primitives.Ints;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.ArrayBlockingQueue;

import java.util.stream.IntStream;


public class ImprovedRecursiveSolver extends RecursiveSolver {

    private static ArrayList<Thread> threads;
    private static ArrayBlockingQueue<Job> jobs;

    private class Job {

        private int i;
        private int v0;
        private final int[] tmpMap;
        private Graph G;
        private TIntArrayList A;
        private TIntIterator it;
        private BitSet removed;

        public Job(int i, int v0, final int[] tmpMap, Graph G, TIntArrayList A, TIntIterator it, BitSet removed) throws InterruptedException{
            this.i = i;
            this.v0 = v0;
            this.tmpMap = tmpMap;
            this.G = G;
            this.A = A;
            this.it = it;
            this.removed = removed;
            jobs.put(this);
        }

        public void execute(){
            if (!removed.get(v0)) {
                boolean flag = G.getPlayerOf(v0) == i;
                if (tmpMap[v0] == 0) {
                    if (flag) {
                        A.add(v0); //azz
                        tmpMap[v0] = 1;
                    } else {
                        int adjCounter = 0;
                        it = G.outgoingEdgesOf(v0).iterator();
                        while (it.hasNext()) {
                            if (!removed.get(it.next())) {
                                adjCounter += 1;
                            }
                        }
                        tmpMap[v0] = adjCounter;
                        if (adjCounter == 1) {
                            A.add(v0); //azz
                        }
                    }
                } else if (!flag && tmpMap[v0] > 1) {
                    tmpMap[v0] -= 1;
                    if (tmpMap[v0] == 1) {
                        A.add(v0); //azz
                    }
                }
            }

        }

    }

    public ImprovedRecursiveSolver(){
        if (threads == null){
            int cores = Runtime.getRuntime().availableProcessors();
            threads = new ArrayList<>(cores);
            jobs = new ArrayBlockingQueue<>(cores);

            for (int i =0 ; i < cores; i++) threads.add(new Thread(){
                public void run(){
                    try {
                        while (!Thread.currentThread().isInterrupted())
                            jobs.take().execute();
                    } catch(InterruptedException e){
                        throw new RuntimeException("Thread InterruptedException");
                    }
                }
            });
            threads.stream().forEach(Thread::start);
        }
    }

    private TIntArrayList Attr(Graph G, TIntArrayList A, int i, BitSet removed) {
        final int[] tmpMap = new int[G.length()];
        TIntIterator it = A.iterator();
        while (it.hasNext()) {
            tmpMap[it.next()] = 1;
        }
        int index = 0;
        while (index < A.size()) {
            final TIntIterator iter = G.incomingEdgesOf(A.get(index)).iterator();
            try {
                while (iter.hasNext()) {
                    int v0 = iter.next();
                    //jobs fai
                    jobs.add(new Job(i, v0, tmpMap, G, A, it, removed));
                }
                index += 1;
                while (jobs.size() != 0) ;
            } catch (InterruptedException e){
                throw new RuntimeException("Attr InterruptedException");
            }
        }
        IntStream.of(A.toArray()).forEach(removed::flip);
        return A;
    }

}