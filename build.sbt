name := "CheckLinks"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.2",
  "org.apache.httpcomponents" % "httpclient" % "4.5.1",
  "org.mongodb" %% "casbah" % "3.0.0"
)
