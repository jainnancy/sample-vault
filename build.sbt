name := "sample-vault"

version := "0.1"

scalaVersion := "2.13.0"

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.0-M4"

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-http
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.8"

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-http-spray-json
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.8"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.23"

//log4j
libraryDependencies += "log4j" % "log4j" % "1.2.17"
