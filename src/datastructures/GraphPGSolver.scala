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

package datastructures

import scala.collection.mutable.ArrayBuffer


class GraphPGSolver extends Cloneable {
  var nodes = Array[Node]()

  def this(numNodes: Int) {
    this()
    nodes = Array.fill[Node](numNodes)(new Node)
  }

  def d() : (Int, ArrayBuffer[Int]) = {
    val A = ArrayBuffer[Int]()
    val max = nodes.maxBy(_.priority).priority
    nodes.zipWithIndex.withFilter(p => p._1.priority == max).foreach(p => A += p._2)
    (max, A)
  }

  def ordering(t2: (Int, Int)) = t2._2

  def addNode(node: Int, parity: Int, player: Int) {
    nodes(node).priority = parity
    nodes(node).player = player
  }

  def addEdge(origin: Int, destination: Int) {
    nodes(origin).adj += destination
  }

  def --(set: ArrayBuffer[Int]) : GraphPGSolver = {
    val G : GraphPGSolver = new GraphPGSolver(this.nodes.length)
    nodes.zipWithIndex.withFilter(_._1.priority != -1).foreach(x => {
      G.nodes(x._2).priority = x._1.priority
      G.nodes(x._2).player = x._1.player
      G.nodes(x._2).adj = x._1.adj
    })
    set.foreach(x => {
      G.nodes(x).priority = -1
    })
    G.nodes.withFilter(_.priority != -1).foreach(x => {
      x.adj.filter(G.nodes(_).priority != -1)
    })
    G
  }

  def transpose() : GraphPGSolver = {
    val G : GraphPGSolver = new GraphPGSolver(this.nodes.length)
    var i = 0
    while (i < G.nodes.length) {
      val x = nodes(i)
      if (x.priority != -1) {
        G.nodes(i).priority = x.priority
        G.nodes(i).player = x.player
        x.adj.foreach(f => {
          G.nodes(f).adj += i
        })
      }
      i += 1
    }
    G
  }

  class Node {
    var priority = -1
    var player = -1
    var adj = ArrayBuffer[Int]()
  }
}