name := "webcrawler"

version := "0.1"

scalaVersion := "2.12.10"

lazy val akkaVersion = "2.5.32"
lazy val scalaScraper = "2.0.0"
lazy val pureConfigVersion = "0.16.0"
lazy val scalatestVersion = "3.2.6"
lazy val scalaCheckVersion = "1.15.2"

val dependencies = {
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "net.ruippeixotog" %% "scala-scraper" % scalaScraper,
    "org.typelevel" %% "cats-core" % "1.5.0",
    "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % scalatestVersion % Test,
    "org.scalacheck" %% "scalacheck" % scalaCheckVersion % Test
  )
}

//def setForkedWorkingDirectory: Seq[Setting[_]] = {
//  // When we fork subprocesses, use the base directory as the working directory.
//  // This enables `sbt> partest test/files/run/t1.scala` or `sbt> scalac sandbox/test.scala`
//  val setting = (Compile / forkOptions) := (Compile / forkOptions).value.withWorkingDirectory((ThisBuild / baseDirectory).value)
//  setting ++ inTask(run)(setting)
//}

lazy val root = (project in file("."))
  .settings(
    name := "webcrawler",
    libraryDependencies ++= dependencies
  ).settings(assembly / assemblyJarName := "webcrawler.jar")

Compile / unmanagedSourceDirectories += baseDirectory.value / "downloaded-pages"
//javaOptions in runMain += "-Dplay.http.secret.key=mysupersecretkey"
