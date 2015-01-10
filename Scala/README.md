Scala Parity Games Solver
=========

An application framework written in Scala for solving Parity Games. The framework supports "Random Games", "Jurdzinski Games", "Clique Games", "Ladder Games" and "Model Checker Ladder Games" generated with PGSolver.

Download
---------
- Source Files can be downloaded [here](https://github.com/vinceprignano/SPGSolver/archive/master.zip)
- Executable Jar can be found [here](https://github.com/vinceprignano/SPGSolver/blob/master/SPGSolver-assembly-1.0.jar?raw=true)

Requirements
---------
- Scala 2.10.4
- [SBT](http://www.scala-sbt.org/)

Building SPGSolver
---------
``` sbt build ``` to build the framework
``` sbt assembly ``` to create a new jar file

Running SPGSolver
---------
```
java -jar SPGSolver-assembly-1.0.jar [options] <file_path>
```
SPGSolver can run with the classic version of the Algorithm using the ``` slower -1 ``` flag in options.

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
