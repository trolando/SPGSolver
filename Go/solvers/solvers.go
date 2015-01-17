package solvers

import (
	"graphs"
	"time"
	"utils"
)

// Solver interface defines only a Win function
// that takes a graph as input and returns the solution
// for player 0 and player 1 as a tuple of integer slices
type Solver interface {
	Win(graph *graphs.Graph) ([]int, []int)
}

// Solve function is exported letting users to
// extend the solvers of this tool using and adaptation
// of the well known Strategy Pattern in Go
func Solve(strategy Solver, graph *graphs.Graph) ([]int, []int) {
	// Prints the time (in seconds) when the function returns
	defer utils.TimeTrack(time.Now(), "Solved")
	return strategy.Win(graph)
}
