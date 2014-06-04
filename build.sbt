import sbtassembly.Plugin.AssemblyKeys
import AssemblyKeys._

assemblySettings

name := "SPGSolver"

version := "1.0"

scalaVersion := "2.10.4"

scalaSource in Compile := baseDirectory.value / "src"

scalacOptions += "-optimize"


libraryDependencies += "net.sf.trove4j" % "trove4j" % "3.0.3"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.2.0"

resolvers += Resolver.sonatypeRepo("public")