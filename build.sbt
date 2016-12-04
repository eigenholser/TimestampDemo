name := "timestamp_demo"

version := "0.1"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.0.0",
  "com.zaxxer" % "HikariCP-java6" % "2.3.2",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.slf4j" % "slf4j-nop" % "1.6.4",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test"
)

libraryDependencies ~= {
  _.map(_.exclude("org.slf4j", "slf4j-nop"))
}

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last endsWith "io.netty.versions.properties" =>
    MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

resolvers += Resolver.sonatypeRepo("public")

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")
scalacOptions ++= Seq(
  "-unchecked",
  "-encoding",
  "utf8",
  "-feature",
  "-language:postfixOps",
  "-Xlint",
  "-Xfatal-warnings",
  "-deprecation",
  "-Xlint:missing-interpolator",
  "-Ywarn-unused-import",
  "-Ywarn-unused",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen"
)
