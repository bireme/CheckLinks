name := "CheckLinks"

version := "1.0"

scalaVersion := "2.12.8"

val akkaVersion =  "2.5.21" //"2.5.17"
val httpclientVersion = "4.5.7" //"4.5.6"
val casbahVersion = "3.1.1"
val hairyfotrVersion = "0.1.17"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "org.apache.httpcomponents" % "httpclient" % httpclientVersion,
  "org.mongodb" %% "casbah" % casbahVersion
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Ywarn-unused")
addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % hairyfotrVersion)
