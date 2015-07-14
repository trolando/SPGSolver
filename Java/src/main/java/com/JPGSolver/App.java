package com.JPGSolver;

import com.beust.jcommander.JCommander;
import com.google.common.base.Stopwatch;

import java.util.Arrays;

public class App {

    public static void main( String[] args ) {
        CommandLineArgs cli = new CommandLineArgs();
        new JCommander(cli, args);

        Stopwatch sw1 = Stopwatch.createStarted();
        Graph G = Graph.initFromFile(cli.files.get(0));
        sw1.stop();
        System.out.println("Parsed in " + sw1);
        Solver solver = cli.parallel ? new ImprovedRecursiveSolver() : new RecursiveSolver();

        Stopwatch sw2 = Stopwatch.createStarted();
        int[][] solution = solver.win(G);
        sw2.stop();
        System.out.println("Solved in " + sw2);

        if (cli.iterative) {
            Solver solver1 = new IterativeSolver();
            Stopwatch sw3 = Stopwatch.createStarted();
            int[][] solution1 = solver1.win(G);
            sw2.stop();
            System.out.println("Solved iteratively in " + sw3);
            if (!Arrays.deepEquals(solution, solution1)){
                System.out.println("Solutions do NOT match! ");
            }
        }
        if (cli.justHeat) {
            return;
        }

        Arrays.sort(solution[0]);
        Arrays.sort(solution[1]);

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
}
