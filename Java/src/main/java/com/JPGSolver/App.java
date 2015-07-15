package com.JPGSolver;

import com.beust.jcommander.JCommander;
import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import java.util.Arrays;

public class App {

    public static void main( String[] args ) {
        CommandLineArgs cli = new CommandLineArgs();
        new JCommander(cli, args);

        Stopwatch sw1 = Stopwatch.createStarted();
        Graph G = Graph.initFromFile(cli.files.get(0));
        sw1.stop();
        System.out.println("Parsed in " + sw1);
        Solver solver = cli.parallel ? new ImprovedRecursiveSolver() : cli.iterative ?new IterativeSolver() : new RecursiveSolver();

        Stopwatch sw2 = Stopwatch.createStarted();
        int[][] solution = solver.win(G);
        sw2.stop();
        System.out.println("Solved in " + sw2);

        if (cli.justHeat) {
            return;
        }

        Arrays.sort(solution[0]);
        Arrays.sort(solution[1]);

        printSolution(solution);
    }

    public static void printSolution(int[][] solution){
        System.out.print("\nSolution for player 0:\n{");
        int index = 0;
        for (int x : solution[0]) {
            if (index == solution[0].length-1) {
                System.out.printf(x + "}");
            } else {
                System.out.printf(x + ", ");
            }
            index += 1;
        }

        System.out.print("\nSolution for player 1:\n{");
        index = 0;
        for (int x : solution[1]) {
            if (index == solution[1].length-1) {
                System.out.printf(x + "}");
            } else {
                System.out.printf(x + ", ");
            }
            index += 1;
        }
        System.out.println();
    }

    public static boolean checkSolution(int[][] s1, int[][] s2){
        for (int x : s1[0]) {
            if (!Ints.contains(s2[0], x)) return false;
        }
        for (int x : s1[1]) {
            if (!Ints.contains(s2[1], x)) return false;
        }
        return true;
    }
}
