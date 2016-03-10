package com.JPGSolver;

import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

import java.util.BitSet;


public class RecursiveSolver implements Solver {

    public Stopwatch swAttr;
    public Stopwatch swTot;

    @Override
    public int[][] win(Graph G) {
        swAttr = Stopwatch.createUnstarted();
        swTot = Stopwatch.createUnstarted();
        BitSet removed = new BitSet(G.length());
        swTot.start();
        int[][]result = win_improved(G, removed);
        swTot.stop();
        return result;
    }

    protected TIntArrayList Attr(Graph G, TIntArrayList A, int i, BitSet removed) {
        final int[] tmpMap = new int[G.length()];
        TIntIterator it = A.iterator();
        while (it.hasNext()) {
            tmpMap[it.next()] = 1;
        }
        int index = 0;
        swAttr.start();
        while (index < A.size()) {
            final TIntIterator iter = G.incomingEdgesOf(A.get(index)).iterator();
            while(iter.hasNext()) {
                int v0 = iter.next();
                if (!removed.get(v0)) {
                    boolean flag = G.getPlayerOf(v0) == i;
                    if (tmpMap[v0] == 0) {
                        if (flag) {
                            A.add(v0);
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
            index += 1;
        }
        swAttr.stop();
        it = A.iterator();
        while (it.hasNext()) {
            removed.set(it.next());
        }
        return A;
    }

    private int[][] win_improved(Graph G, BitSet removed) {
        final int[][] W = {new int[0], new int[0]};
        final int d = G.maxPriority(removed);
        if (d > -1) {
            TIntArrayList U = G.getNodesWithPriority(d, removed);
            final int p = d % 2;
            final int j = 1 - p;
            int[][] W1;
            BitSet removed1 = (BitSet)removed.clone();
            final TIntArrayList A = Attr(G, U, p, removed1);
            W1 = win_improved(G, removed1);
            if (W1[j].length == 0) {
                W[p] = Ints.concat(W1[p], A.toArray());
            } else {
                BitSet removed2 = (BitSet)removed.clone();
                final TIntArrayList B = Attr(G, new TIntArrayList(W1[j]), j, removed2);
                W1 = win_improved(G, removed2);
                W[p] = W1[p];
                W[j] = Ints.concat(W1[j], B.toArray());
            }
        }
        return W;
    }
}
