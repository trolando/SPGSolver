package solvers

import (
	"sync"
	"sync/atomic"
	"time"

	"github.com/vinceprignano/SPGSolver/Go/graphs"
	"github.com/vinceprignano/SPGSolver/Go/utils"
)

type ConcurrentSolver struct{}

// win is the main function in Improved Zielonka's Recursive Algorithm
func (c *ConcurrentSolver) win(G *graphs.Graph, removed []bool) ([]int, []int) {
	var W [2][]int
	d := G.MaxPriority(removed)
	if d > -1 {
		U := []int{}
		for _, v := range G.PriorityMap[d] {
			if !removed[v] {
				U = append(U, v)
			}
		}
		p := d % 2
		j := 1 - p
		var W1 [2][]int
		A := c.attr(G, removed, U, p)
		removed1 := make([]bool, len(removed))
		copy(removed1, removed)
		for _, x := range A {
			removed1[x] = true
		}
		W1[0], W1[1] = c.win(G, removed1)
		if len(W1[j]) == 0 {
			W[p] = append(W1[p], A...)
		} else {
			B := c.attr(G, removed, W1[j], j)
			removed2 := make([]bool, len(removed))
			copy(removed2, removed)
			for _, x := range B {
				removed2[x] = true
			}
			W1[0], W1[1] = c.win(G, removed2)
			W[p] = W1[p]
			W[j] = append(W1[j], B...)
		}
	}
	return W[0], W[1]
}

func (c *ConcurrentSolver) attrHelper(G *graphs.Graph, removed []bool, tmpMap []int32, flags []uint32, ch chan int, i int, node int, wg *sync.WaitGroup) {
	for _, v0 := range G.Nodes[node].Inc {
		if !removed[v0] {
			flag := G.Nodes[v0].Player == i
			if atomic.CompareAndSwapUint32(&flags[v0], 0, 1) {
				if flag {
					ch <- v0
					atomic.AddInt32(&tmpMap[v0], 1)
				} else {
					adj_counter := 0
					for _, x := range G.Nodes[v0].Adj {
						if !removed[x] {
							adj_counter += 1
						}
					}
					atomic.AddInt32(&tmpMap[v0], int32(adj_counter))
					if adj_counter == 1 {
						ch <- v0
					}
				}
			} else if !flag {
				if atomic.AddInt32(&tmpMap[v0], -1) == 1 {
					ch <- v0
				}
			}
		}
	}
	wg.Done()
}

func (c *ConcurrentSolver) attr(G *graphs.Graph, removed []bool, A []int, i int) []int {
	checkSet := make([]bool, len(G.Nodes))
	tmpMap := make([]int32, len(G.Nodes))
	flags := make([]uint32, len(G.Nodes))
	for _, x := range A {
		checkSet[x] = true
		tmpMap[x] = 1
		flags[x] = 1
	}
	index := 0
	for {
		wg := &sync.WaitGroup{}
		ch := make(chan int, len(G.Nodes))

		for {
			wg.Add(1)
			go c.attrHelper(G, removed, tmpMap, flags, ch, i, A[index], wg)

			index += 1
			if index == len(A) {
				break
			}
		}

		go func() {
			wg.Wait()
			close(ch)
		}()

		for res := range ch {
			if checkSet[res] == false {
				A = append(A, res)
				checkSet[res] = true
			}
		}

		if index == len(A) {
			break
		}
	}
	return A
}

// Win is implemented by RecursiveImproved and returns
// the solution for a given game in input
func (c *ConcurrentSolver) Win(G *graphs.Graph) ([]int, []int) {
	defer utils.TimeTrack(time.Now(), "Solved with Concurrent")
	removed := make([]bool, len(G.Nodes))
	res1, res2 := c.win(G, removed)
	return res1, res2
}
