addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

name := "workshop"

version := "0.1"

scalaVersion := "2.12.7"

libraryDependencies += "com.github.mpilquist" %% "simulacrum" % "0.14.0"
libraryDependencies += "org.scalatest" % "scalatest_2.12" % "3.0.5" % "test"
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % "test"