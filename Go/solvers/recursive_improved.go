package solvers

import "github.com/vinceprignano/SPGSolver/Go/graphs"

type RecursiveImproved struct{}

// attr is the attractor function used by 'win'
func attr(G *graphs.Graph, removed []bool, A []int, i int) []int {
	tmpMap := make([]int, len(G.Nodes()))
	for _, x := range A {
		tmpMap[x] = 1
	}
	index := 0
	for {
		for _, v0 := range G.Nodes()[A[index]].Inj() {
			if !removed[v0] {
				flag := G.Nodes()[v0].Player() == i
				if tmpMap[v0] == 0 {
					if flag {
						A = append(A, v0)
						tmpMap[v0] = 1
					} else {
						adj_counter := 0
						for _, x := range G.Nodes()[v0].Adj() {
							if !removed[x] {
								adj_counter += 1
							}
						}
						tmpMap[v0] = adj_counter
						if adj_counter == 1 {
							A = append(A, v0)
						}
					}
				} else if !flag && tmpMap[v0] > 1 {
					tmpMap[v0] -= 1
					if tmpMap[v0] == 1 {
						A = append(A, v0)
					}
				}
			}
		}
		index += 1
		if index == len(A) {
			break
		}
	}
	return A
}

// win is the main function in Improved Zielonka's Recursive Algorithm
func win(G *graphs.Graph, removed []bool) ([]int, []int) {
	var W [2][]int
	d := G.MaxPriority(removed)
	if d > -1 {
		U := []int{}
		for _, v := range G.Priorities()[d] {
			if !removed[v] {
				U = append(U, v)
			}
		}
		p := d % 2
		j := 1 - p
		var W1 [2][]int
		A := attr(G, removed, U, p)
		removed1 := make([]bool, len(removed))
		copy(removed1, removed)
		for _, x := range A {
			removed1[x] = true
		}
		W1[0], W1[1] = win(G, removed1)
		if len(W1[j]) == 0 {
			W[p] = append(W1[p], A...)
		} else {
			B := attr(G, removed, W1[j], j)
			removed2 := make([]bool, len(removed))
			copy(removed2, removed)
			for _, x := range B {
				removed2[x] = true
			}
			W1[0], W1[1] = win(G, removed2)
			W[p] = W1[p]
			W[j] = append(W1[j], B...)
		}
	}
	return W[0], W[1]
}

// Win is implemented by RecursiveImproved and returns
// the solution for a given game in input
func (r RecursiveImproved) Win(G *graphs.Graph) ([]int, []int) {
	removed := make([]bool, len(G.Nodes()))
	res1, res2 := win(G, removed)
	return res1, res2
}
