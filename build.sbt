name := "concurrency_in_scala"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.scalacheck"  %% "scalacheck"  % "1.13.5",
  "org.specs2"      %% "specs2-scalacheck" % "4.0.3",
  "org.specs2"      %% "specs2-core"  % "4.0.3"
)

fork := true