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

import gnu.trove.list.array.TIntArrayList
import gnu.trove.map.hash.TIntObjectHashMap
import scala.collection.mutable.ArrayBuffer


object Graph {
  def apply() : Graph = {
    new Graph()
  }

  def apply(nodes : Int) : Graph = {
    new Graph(nodes)
  }

  class Node {
    var player = -1
    var priority = -1
    val adj = new TIntArrayList()
    val inj = new TIntArrayList()

    lazy val ~> = adj.toArray
    lazy val <~ = inj.toArray
  }
}


class Graph extends Cloneable {
  val priorityMap = new TIntObjectHashMap[ArrayBuffer[Int]]()
  var nodes = Array[Graph.Node]()
  var exclude : Array[Boolean] = null

  def this(numNodes: Int) {
    this()
    nodes = Array.fill[Graph.Node](numNodes)(new Graph.Node)
    exclude = Array.fill[Boolean](numNodes)(false)
  }

  def d() : Int = {
    var max = -1
    var index = 0
    for (v <- nodes) {
      if (!exclude(index) && v.priority > max) {
        max = v.priority
      }
      index += 1
    }
    max
  }

  def addNode(node: Int, parity: Int, player: Int) {
    nodes(node).player = player
    nodes(node).priority = parity
    if (priorityMap.containsKey(parity)) {
      priorityMap.get(parity).+=(node)
    } else {
      priorityMap.put(parity, new ArrayBuffer[Int]())
      priorityMap.get(parity).+=(node)
    }
  }

  def addEdge(origin: Int, destination: Int) {
    nodes(origin).adj add destination
    nodes(destination).inj add origin
  }

  def --(set: ArrayBuffer[Int]) : Graph = {
    val G : Graph = this.clone().asInstanceOf[Graph]
    G.exclude = this.exclude.clone()
    set.foreach(x => {
      G.exclude(x) = true
    })
    G
  }
}