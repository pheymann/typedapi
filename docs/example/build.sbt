
scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "com.github.pheymann" %% "typedapi-http4s-client" % "0.1.0-RC5",
  "com.github.pheymann" %% "typedapi-http4s-server" % "0.1.0-RC5",

  "org.http4s" %% "http4s-blaze-client" % "0.18.0",
  "org.http4s" %% "http4s-blaze-server" % "0.18.0",
  "org.http4s" %% "http4s-dsl" % "0.18.0",

  "org.http4s" %% "http4s-circe" % "0.18.0",
  "io.circe"   %% "circe-core" % "0.9.1",
  "io.circe"   %% "circe-parser" % "0.9.1",
  "io.circe"   %% "circe-generic" % "0.9.1"
)
