organization := "com.consideredgames"
name := "vv-server"

version := "1.0"

scalaVersion := "2.11.11"

resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
resolvers += Resolver.bintrayRepo("commercetools", "maven")
resolvers += Resolver.bintrayRepo("hseeberger", "maven")
resolvers += Resolver.mavenLocal

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.7"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

libraryDependencies += "org.jasypt" % "jasypt" % "1.9.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.6",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.6" % Test
)
libraryDependencies += "de.heikoseeberger" %% "akka-http-json4s" % "1.14.0"
libraryDependencies += "org.json4s" %% "json4s-native" % "3.5.2"

libraryDependencies += "com.consideredgames" %% "vv-lib" % "0.1-SNAPSHOT"

libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0"

//libraryDependencies += "io.sphere" %% "sphere-json" % "0.6.10"

//val circeVersion = "0.7.0"
//libraryDependencies ++= Seq(
//  "io.circe" %% "circe-core",
//  "io.circe" %% "circe-generic",
//  "io.circe" %% "circe-parser"
//).map(_ % circeVersion)

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"