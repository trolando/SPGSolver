package main

import (
	"flag"
	"fmt"
	"os"
	"sort"

	"github.com/vinceprignano/SPGSolver/Go/graphs"
	"github.com/vinceprignano/SPGSolver/Go/solvers"
)

func main() {

	// Flags for command line
	justHeatFlag := flag.Bool("justheat", false, "Pass this option to disable solution printing.")
	help := flag.Bool("help", false, "Prints this help screen.")
	flag.Parse()

	// Checking for help or if nothing was given as file
	if *help || len(flag.Args()) == 0 {
		fmt.Fprintf(os.Stderr, "usage: %s -graph [options ...] inputfile\n", os.Args[0])
		flag.PrintDefaults()
		os.Exit(2)
	}

	// Parsing the Graph through the default
	// way using the file that respets the PGSolver's format
	G := graphs.NewGraphFromPGSolverFile()

	// Declaring two solutions slices of integers
	var solution0, solution1 []int

	// Starting solving process, using Recursive Improved as default method
	// Solving the game using the Recursive Improved Algorithm
	solution0, solution1 = solvers.Solve(solvers.RecursiveImproved{}, G)

	if !*justHeatFlag {
		// Sorting the solutions
		sort.Ints(solution0)
		sort.Ints(solution1)

		// Printing solutions in a nice way
		fmt.Println("\nPlayer 0 wins from nodes:")
		fmt.Printf("{")
		for i := 0; i < len(solution0); i++ {
			if i == len(solution0)-1 {
				fmt.Printf("%d}", solution0[i])
			} else {
				fmt.Printf("%d, ", solution0[i])
			}
		}
		fmt.Printf("\n\n")
		fmt.Println("Player 1 wins from nodes:")
		fmt.Printf("{")
		for i := 0; i < len(solution1); i++ {
			if i == len(solution1)-1 {
				fmt.Printf("%d}", solution1[i])
			} else {
				fmt.Printf("%d, ", solution1[i])
			}
		}
		fmt.Printf("\n")
	}
}
