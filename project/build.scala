import sbt._

object Dependencies {

  val client = Seq(
    "com.chuusai" %% "shapeless" % "2.3.3" % Compile,

    "org.specs2"  %% "specs2-core" % "3.9.4" % Test
  )

  private val http4sV = "0.18.0"
  private val circeV  = "0.9.1"

  val http4s = Seq(
    "org.http4s" %% "http4s-blaze-client" % http4sV % Provided,

    "org.http4s"      %% "http4s-blaze-server" % http4sV % "it",
    "org.http4s"      %% "http4s-dsl" % http4sV % "it",
    "org.http4s" %% "http4s-circe" % http4sV        % "it",
    "io.circe" %% "circe-core" % circeV    % "it",
    "io.circe" %% "circe-parser" % circeV  % "it",
    "io.circe" %% "circe-generic" % circeV % "it",
    "org.specs2"  %% "specs2-core" % "3.9.4" % "it"
  )
}
