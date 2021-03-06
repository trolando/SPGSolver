SPGSolver
=========

A collection of tools written in multiple programming languages to solve parity games.

> A parity game is played on a colored directed graph, where each node has been colored by a priority – one of (usually) finitely many natural numbers. Two players, 0 and 1, move a (single, shared) token along the edges of the graph. The owner of the node that the token falls on, selects the successor node, resulting in a (possibly infinite) path, called the play. The winner of a finite play is the player whose opponent is unable to move. The winner of an infinite play is determined by the priorities appearing in the play. Typically, player 0 wins an infinite play if the largest priority that occurs infinitely often in the play is even. Player 1 wins otherwise. This explains the word "parity" in the title.

###### Example

![](http://upload.wikimedia.org/wikipedia/commons/3/31/Example_Parity_Game_Solved.png)

A parity game. Circular nodes belong to player 0, rectangular nodes belong to player 1.
On the left side is the winning region of player 0, on the right side is the winning region of player 1.

###### Comparison
![](https://raw.githubusercontent.com/vinceprignano/SPGSolver/master/comparison.png)

License
---------

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
