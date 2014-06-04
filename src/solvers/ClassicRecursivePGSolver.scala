/*
  The MIT License (MIT)

  Copyright (c) 2014 Vincenzo Prignano

  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:

  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.

*/


package solvers

import scala.collection.{mutable => m}
import scala.collection.mutable.ArrayBuffer
import datastructures.GraphPGSolver

object ClassicRecursivePGSolver {

  private def Attr(G: GraphPGSolver, A: ArrayBuffer[Int], i: Int) : ArrayBuffer[Int] = {
    val B = m.TreeSet[Int](A:_*)
    val queue = m.Queue[Int](A:_*)
    val T = G.transpose()
    while (!queue.isEmpty) {
      T.nodes(queue.dequeue()).adj.foreach(v0 => {
        if (!B.contains(v0)) {
          if (G.nodes(v0).player == i) {
            B += v0
            queue.enqueue(v0)
          } else if (G.nodes(v0).adj.forall(B.contains)) {
            B += v0
            queue.enqueue(v0)
          }
        }
      })
    }
    ArrayBuffer(B.toArray:_*)
  }


  def win(G: GraphPGSolver) : Array[Int] = {
    val W = Array.fill[Int](G.nodes.length)(-1)
    if (G.nodes.count(_.priority == -1) == G.nodes.length) {
      return Array.fill[Int](G.nodes.length)(-1)
    }
    val d = G.d()
    val p = d._1 % 2
    val j = if (p == 0) 1 else 0
    var W1 = Array.fill[Int](G.nodes.length)(-1)
    val A = Attr(G, d._2, p)
    W1 = win(G -- A)
    if (W1.count(v => v == j) == 0) {
      W1.zipWithIndex.withFilter(v => G.nodes(v._2).priority != -1).foreach(f => {
        W(f._2) = p
      })
    } else {
      val tmp = ArrayBuffer[Int]()
      W1.zipWithIndex.filter(_._1 == j).foreach(f => {
        tmp += f._2
      })
      val B = Attr(G, tmp, j)
      W1 = win(G -- B)
      B.foreach(f => {
        W(f) = j
      })
      W1.zipWithIndex.foreach(f => {
        if (f._1 == p) {
          W(f._2) = p
        } else if (f._1 == j) {
          W(f._2) = j
        }
      })
    }
    W
  }
}