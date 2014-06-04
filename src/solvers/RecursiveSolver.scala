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

import scala.collection.mutable.ArrayBuffer
import datastructures.Graph

object RecursiveSolver extends Solver {

  private def Attr(G: Graph, A: ArrayBuffer[Int], i: Int) : ArrayBuffer[Int] = {
    val tmpMap = Array.fill[Int](G.nodes.size)(-1)
    var index = 0
    A.foreach(tmpMap(_) = 0)
    while (index < A.size) {
      G.nodes(A(index)).<~.foreach(v0 => {
        if (!G.exclude(v0)) {
          val flag = G.nodes(v0).player == i
          if (tmpMap(v0) == -1) {
            if (flag) {
              A += v0
              tmpMap(v0) = 0
            } else {
              val tmp = G.nodes(v0).~>.count(x => !G.exclude(x)) - 1
              tmpMap(v0) = tmp
              if (tmp == 0) A += v0
            }
          } else if (!flag && tmpMap(v0) > 0){
            tmpMap(v0) -= 1
            if (tmpMap(v0) == 0) A += v0
          }
        }
      })
      index += 1
    }
    A
  }

  override def win(G: Graph) : (ArrayBuffer[Int], ArrayBuffer[Int]) = {
    val W = Array(ArrayBuffer.empty[Int], ArrayBuffer.empty[Int])
    val d = G.d()
    if (d > -1) {
      val U = G.priorityMap.get(d).filter(p => !G.exclude(p))
      val p = d % 2
      val j = 1 - p
      val W1 = Array(ArrayBuffer.empty[Int], ArrayBuffer.empty[Int])
      val A = Attr(G, U, p)
      val res = win(G -- A)
      W1(0) = res._1
      W1(1) = res._2
      W1(j).size match {
        case 0 =>
          W(p) = W1(p) ++= A
          W(j) = ArrayBuffer.empty[Int]
        case _ =>
          val B = Attr(G, W1(j), j)
          val res2 = win(G -- B)
          W1(0) = res2._1
          W1(1) = res2._2
          W(p) = W1(p)
          W(j) = W1(j) ++= B
      }
    }
    (W(0), W(1))
  }
}
