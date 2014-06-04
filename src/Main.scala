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

import datastructures.{GraphPGSolver, Graph}
import scala.collection.mutable.ArrayBuffer
import solvers.{ConcreteSolver, RecursiveSolver, ClassicRecursivePGSolver}
import Utils.time

import java.io.File
case class Config(files: Seq[File] = Seq(), slower: Boolean = false)

object Main {

  private var G : Graph = null
  private var G1 : GraphPGSolver = null

  def initWithSetsFromFileSlower(f : File) : GraphPGSolver  = {
    var nodes = -1
    val iter = io.Source.fromFile(f.getAbsolutePath).getLines()
    if (iter.hasNext) {
      val ln = iter.next().split(" ")
      if (!ln.contains("parity")) {
        throw new RuntimeException("Invalid file passed.")
      }
      nodes = ln(1).dropRight(1).toInt + 1
    }
    val G = new GraphPGSolver(nodes)
    for (ln <- io.Source.fromFile(f.getAbsolutePath).getLines()) {
      if (!ln.startsWith("parity")) {
        val x = ln.split(" ")
        val (_, edges) = x.splitAt(3)
        G.addNode(x(0).toInt, x(1).toInt, x(2).toInt)
        for (edge <- edges(0).split(",")) {
          if (edge.endsWith(";")) {
            G.addEdge(x(0).toInt, edge.dropRight(1).toInt)
          } else {
            G.addEdge(x(0).toInt, edge.toInt)
          }
        }
      }
    }
    G
  }

  def initWithSetsFromFile(f : File) : Graph  = {
    var nodes = -1
    val iter = io.Source.fromFile(f.getAbsolutePath).getLines()
    if (iter.hasNext) {
      val ln = iter.next().split(" ")
      if (!ln.contains("parity")) {
        throw new RuntimeException("Invalid file passed.")
      }
      nodes = ln(1).dropRight(1).toInt + 1
    }
    val G = new Graph(nodes)
    for (ln <- iter) {
      val x = ln.split(" ")
      val (_, edges) = x.splitAt(3)
      G.addNode(x(0).toInt, x(1).toInt, x(2).toInt)
      for (edge <- edges(0).split(",")) {
        if (edge.endsWith(";")) {
          G.addEdge(x(0).toInt, edge.dropRight(1).toInt)
        } else {
          G.addEdge(x(0).toInt, edge.toInt)
        }
      }
    }
    G.nodes.zipWithIndex.foreach(x => {
      if (x._1.priority == -1 ) {
        G.exclude(x._2) = true
      }
    })
    G
  }


  def main(args: Array[String]) {
    val parser = new scopt.OptionParser[Config]("scopt") {
      help("help") text "prints this usage text"
      arg[File]("<file>") unbounded() action { (x, c) =>
        c.copy(files = c.files :+ x) } text "Graph File"
      opt[Boolean]("slower") abbr "sl" optional() action { (x, c) =>
        c.copy(slower = x)
      }
    }

    parser.parse(args, Config()) map { config =>
      println("Parsing Graph from " + config.files.head.getName)
      if (!config.slower) {
        G = time("Parsed in", initWithSetsFromFile(config.files.head))
        val (player0, player1) = time("Solved in ", ConcreteSolver.solve(G, RecursiveSolver))
        println("Player 0 wins from nodes:")
        println(player0.sorted)
        println("Player 1 wins from nodes:")
        println(player1.sorted)
      } else {
        G1 = time("Parsed in", initWithSetsFromFileSlower(config.files.head))
        println("Chosen Slower Recursive Algorithm...")
        val solution = time("Solved in ", ClassicRecursivePGSolver.win(G1))
        val zero = ArrayBuffer[Int]()
        val uno = ArrayBuffer[Int]()
        solution.zipWithIndex.foreach(f => {
          if (f._1 == 0) zero += f._2
          if (f._1 == 1) uno += f._2
        })
        println("Player 0 wins from nodes:")
        println(zero.sorted)
        println("Player 1 wins from nodes:")
        println(uno.sorted)
      }
    } getOrElse {
      // arguments are bad, error message will have been displayed
    }
  }
}