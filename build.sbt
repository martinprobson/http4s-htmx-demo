val catsEffectVersion = "3.5.2"
val http4sVersion = "0.23.23"
val doobieVersion = "1.0.0-RC1"

val logging = Seq(
  "org.slf4j" % "slf4j-api" % "2.0.5",
  "ch.qos.logback" % "logback-classic" % "1.4.12",
  "ch.qos.logback" % "logback-core" % "1.4.12",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "org.typelevel" %% "log4cats-slf4j" % "2.6.0"
)

val webJars = Seq(
  "org.webjars.npm" % "bootstrap" % "5.3.2",
  "org.webjars.npm" % "htmx.org" % "1.9.10",
  "org.webjars.npm" % "popper.js" % "1.16.1"
)

val db = Seq(
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "mysql" % "mysql-connector-java" % "8.0.30",
  "com.h2database" % "h2" % "1.4.200",
)

val config = Seq( "com.github.japgolly.clearconfig" %% "core" % "3.1.0")

lazy val root = (project in file("."))
  .settings(
    name := "http4s htmx demo",
    scalaVersion := "3.3.1",
    version := "0.0.1-SNAPSHOT",
    run / fork := true,
    scalacOptions ++= Seq(
      "-deprecation", // Emit warning and location for usages of deprecated APIs.
      "-explaintypes", // Explain type errors in more detail.
      "-Xfatal-warnings" // Fail the compilation if there are any warnings.
    ),
    libraryDependencies ++= config,
    libraryDependencies ++= logging,
    libraryDependencies ++= webJars,
    libraryDependencies ++= db,
    libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-effect"          % catsEffectVersion,
        "org.http4s"    %% "http4s-ember-client"  % http4sVersion,
        "org.http4s"    %% "http4s-ember-server"  % http4sVersion,
        "org.http4s"    %% "http4s-dsl"           % http4sVersion,
        "org.http4s"    %% "http4s-twirl"         % "0.24.0-M1"
    )
)
  .enablePlugins(SbtTwirl)

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", "MANIFEST.MF")       => MergeStrategy.discard
  case n if n.startsWith("reference.conf") => MergeStrategy.concat
  case _                                   => MergeStrategy.first
}
