package graphs

import (
	"bufio"
	"flag"
	"fmt"
	"io"
	"os"
	"strconv"
	"strings"
	"time"
	"utils"
)

// Node struct keeps track of adjacent and incident lists.
// Its objects can be related to a vertex in a directed Graph
// data structure.
type node struct {
	adj      []int
	inj      []int
	player   int
	priority int
}

// Returns the node's Successors List
func (n *node) Adj() []int {
	return n.adj
}

// Returns the node's Predecessors List
func (n *node) Inj() []int {
	return n.inj
}

// Returns the node's Player
func (n *node) Player() int {
	return n.player
}

// Returns the node's Priority
func (n *node) Priority() int {
	return n.priority
}

// Graph is the main data structure, it has an array of 'node' objects
// and a map keeping track of every node with a given priority
type Graph struct {
	priorityMap map[int][]int
	nodes       []node
}

// Priorites returns the map of all priorities
func (g *Graph) Priorities() map[int][]int {
	return g.priorityMap
}

// Nodes returns the nodes array
func (g *Graph) Nodes() []node {
	return g.nodes
}

// NewGraph builds a new graph and preallocates all nodes up to the parameter
// 'numnodes' specified as input
func NewGraph(numnodes int) *Graph {
	G := &Graph{
		nodes:       make([]node, numnodes, numnodes),
		priorityMap: make(map[int][]int),
	}
	for i := range G.nodes {
		G.nodes[i] = node{
			adj:      []int{},
			inj:      []int{},
			player:   -1,
			priority: -1,
		}
	}
	return G
}

// NewGraphFromPGSolverFile builds a new graph using a file, given as
// argument, that can be generated through PGSolver's generation tools
// or that respects the very same format.
func NewGraphFromPGSolverFile() *Graph {
	fmt.Println("Parsing Graph from .....", flag.Args()[0])
	defer utils.TimeTrack(time.Now(), "Parsed")
	numnodes := -1
	fds, _ := os.Open(flag.Args()[0])
	defer fds.Close()
	file := bufio.NewReader(fds)
	first, err := file.ReadString((byte)('\n'))
	if strings.Contains(first, "parity") {
		first_split := strings.Split(first, " ")
		numnodes, _ = strconv.Atoi(first_split[1][:len(first_split[1])-2])
	} else if err != nil {
		fmt.Fprintln(os.Stderr, "Error when reading from the file:", err)
	}
	G := NewGraph(numnodes + 1)
	for {
		line, err := file.ReadString((byte)('\n'))
		if err == io.EOF {
			break
		}
		x := strings.Split(line, " ")
		node := x[0:3]
		i, _ := strconv.Atoi(node[0])
		priority, _ := strconv.Atoi(node[1])
		player, _ := strconv.Atoi(node[2])
		G.AddNode(i, priority, player)
		edges := strings.Split(x[3:len(x)][0], ",")
		edges[len(edges)-1] = strings.Replace(edges[len(edges)-1], ";", "", -1)
		edges[len(edges)-1] = strings.Replace(edges[len(edges)-1], "\n", "", -1)
		for _, dest := range edges {
			destination, _ := strconv.Atoi(dest)
			G.AddEdge(i, destination)
		}
	}
	return G
}

// MaxPriority returns the max priority in the graph
// takes a slice of booleans that will be excluded
// when looping through the graph
func (g *Graph) MaxPriority(removed []bool) int {
	max := -1
	for i, _ := range g.nodes {
		if !removed[i] && g.nodes[i].priority > max {
			max = g.nodes[i].priority
		}
	}
	return max
}

// AddNode adds a node to the graph
func (g *Graph) AddNode(newnode int, priority int, player int) {
	g.priorityMap[priority] = append(g.priorityMap[priority], newnode)
	g.nodes[newnode].player = player
	g.nodes[newnode].priority = priority
}

// AddEdge adds a new edge to the graph
func (g *Graph) AddEdge(origin int, destination int) {
	g.nodes[origin].adj = append(g.nodes[origin].adj, destination)
	g.nodes[destination].inj = append(g.nodes[destination].inj, origin)
}
