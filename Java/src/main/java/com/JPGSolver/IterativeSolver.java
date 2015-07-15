package com.JPGSolver;

import com.google.common.primitives.Ints;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;

import java.util.stream.IntStream;


public class IterativeSolver extends RecursiveSolver {

    // (First rule)
    private class Snapshot{

        public BitSet removed;
        public int[][] W;
        public int stage;

        //local variables that will be used after returning from the function call
        public int[][] W1;
        public TIntArrayList A;
        public TIntArrayList B;
        public int j;
        public int p;

        public Snapshot(BitSet bitset, int stage){
            this.removed = bitset;
            this.stage = stage;
        }

    }

    @Override
    public int[][] win(Graph G) {
        BitSet removed = new BitSet(G.length());
        return win_improved(G, removed);
    }

    private int[][] win_improved(Graph G, BitSet removed) {

        int[][] retVal = {new int[0], new int[0]};

        /* -- Recursive Logic -- */
        Stack<Snapshot> stack = new Stack<>();
        Snapshot currentSnapshot = new Snapshot(removed, 0);
        stack.push(currentSnapshot);
        /* --------------------- */

        while (!stack.isEmpty()) {
            currentSnapshot = stack.pop();

            if (currentSnapshot.stage == 0) {

                /* -- Recursive Logic -- */
                removed = currentSnapshot.removed;
                /* --------------------- */

                int d = G.maxPriority(removed);
                int[][] W = {new int[0], new int[0]};
                if (d > -1) {
                    TIntArrayList U = G.getNodesWithPriority(d, removed);
                    int p = d % 2;
                    int j = 1 - p;
                    //int[][] W1;
                    BitSet removed1 = (BitSet) removed.clone();
                    final TIntArrayList A = Attr(G, U, p, removed1);

                    /* -- Recursive Logic -- */
                    currentSnapshot.stage = 1;
                    currentSnapshot.j = j;
                    currentSnapshot.p = p;
                    currentSnapshot.A = A;
                    currentSnapshot.W = W;
                    stack.push(currentSnapshot);
                    stack.push(new Snapshot(removed1, 0));
                    /* --------------------- */
                }
                retVal = W;
            } else if (currentSnapshot.stage == 1) {
                //W1 = win_improved(G, removed1);

                /* -- Recursive Logic -- */
                int[][] W1 = retVal;
                removed = currentSnapshot.removed;
                int j = currentSnapshot.j;
                int p = currentSnapshot.p;
                TIntArrayList A = currentSnapshot.A;
                int[][] W = currentSnapshot.W;
                /* --------------------- */

                if (W1[j].length == 0) {
                    W[p] = Ints.concat(W1[p], A.toArray());
                } else {
                    BitSet removed2 = (BitSet) removed.clone();
                    TIntArrayList B = Attr(G, new TIntArrayList(W1[j]), j, removed2);

                    /* -- Recursive Logic -- */
                    currentSnapshot.stage = 2;
                    currentSnapshot.B = B;
                    currentSnapshot.W1 = W1;
                    stack.push(currentSnapshot);
                    stack.push(new Snapshot(removed2, 0));
                    /* --------------------- */
                }

            } else {
                /* -- Recursive Logic -- */
                int[][] W1 = retVal;
                TIntArrayList B = currentSnapshot.B;
                int[][] W = currentSnapshot.W;
                int p = currentSnapshot.p;
                int j = currentSnapshot.j;
                /* --------------------- */

                W[p] = W1[p];
                W[j] = Ints.concat(W1[j], B.toArray());
            }
        }
        return retVal;
    }

}