name := "CheckLinks"

version := "1.0"

scalaVersion := /*"2.13.7"*/ "2.12.9"  // casbah congelado
val akkaVersion = "2.6.18" //"2.6.15"
val httpclientVersion = "4.5.13" //"4.5.10"
val casbahVersion = "3.1.1"
val scalajVersion = "2.4.2" //"2.4.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "org.apache.httpcomponents" % "httpclient" % httpclientVersion,
  "org.mongodb" %% "casbah" % casbahVersion,
  "org.scalaj" %% "scalaj-http" % scalajVersion
)

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Ywarn-unused")
//addCompilerPlugin("org.psywerx.hairyfotr" %% "linter" % hairyfotrVersion)
