addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

name := "workshop"

version := "0.1"

scalaVersion := "2.12.8"

scalacOptions += "-Ypartial-unification"

val http4sVersion = "0.20.0-SNAPSHOT"
val circeVersion = "0.11.1"
val doobieVersion = "0.6.0"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "com.github.mpilquist"     %% "simulacrum"                % "0.14.0",
  "org.apache.logging.log4j" %  "log4j-slf4j18-impl"        % "2.11.2",
  "org.apache.logging.log4j" %  "log4j-api"                 % "2.11.2",
  "org.http4s"               %% "http4s-dsl"                % http4sVersion,
  "org.http4s"               %% "http4s-blaze-server"       % http4sVersion,
  "org.http4s"               %% "http4s-blaze-client"       % http4sVersion,
  "org.http4s"               %% "http4s-circe"              % http4sVersion,
  // "org.http4s"               %% "http4s-prometheus-metrics" % http4sVersion,
  // Optional for auto-derivation of JSON codecs
  "io.circe"                 %% "circe-generic"             % circeVersion,
  "io.circe"                 %% "circe-literal"             % circeVersion,
  "org.tpolecat"             %% "doobie-core"               % doobieVersion,
  // H2 driver 1.4.197 + type mappings.
  "org.tpolecat"             %% "doobie-h2"                 % doobieVersion,
  "org.scalatest"            %% "scalatest"                 % "3.0.6"  % "test",
  "org.scalacheck"           %% "scalacheck"                % "1.14.0" % "test"
)