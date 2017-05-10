name := "CheckLinks"

version := "1.0"

scalaVersion := "2.12.2"

val akkaVersion = "2.4.18"
val httpclientVersion = "4.5.3"
val casbahVersion = "3.1.1"
val hairyfotrVersion = "0.1.17"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "org.apache.httpcomponents" % "httpclient" % httpclientVersion,
  "org.mongodb" %% "casbah" % casbahVersion
)

addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % hairyfotrVersion)
