import sbt._

object Dependencies {

  val shared = Seq(
    "com.chuusai" %% "shapeless" % "2.3.3" % Compile,

    "org.specs2"  %% "specs2-core" % "3.9.4" % Test
  )

  val client = Seq(
    "org.specs2"  %% "specs2-core" % "3.9.4" % Test
  )

  val server = Seq(
    "org.specs2"  %% "specs2-core" % "3.9.4" % Test
  )

  private val http4sV = "0.18.0"
  private val circeV  = "0.9.1"

  val http4sClient = Seq(
    "org.http4s" %% "http4s-blaze-client" % http4sV % Provided,

    "org.http4s" %% "http4s-blaze-server" % http4sV % Test,
    "org.http4s" %% "http4s-dsl" % http4sV % Test,
    "org.http4s" %% "http4s-circe" % http4sV        % Test,
    "io.circe"   %% "circe-core" % circeV    % Test,
    "io.circe"   %% "circe-parser" % circeV  % Test,
    "io.circe"   %% "circe-generic" % circeV % Test,
    "org.specs2" %% "specs2-core" % "3.9.4" % Test
  )

  val http4sServer = Seq(
    "org.http4s" %% "http4s-blaze-server" % http4sV % Provided,
    "org.http4s" %% "http4s-dsl" % http4sV % Provided,

    "org.http4s" %% "http4s-blaze-client" % http4sV % Test,
    "org.http4s" %% "http4s-circe" % http4sV        % Test,
    "io.circe"   %% "circe-core" % circeV    % Test,
    "io.circe"   %% "circe-parser" % circeV  % Test,
    "io.circe"   %% "circe-generic" % circeV % Test,
    "org.specs2" %% "specs2-core" % "3.9.4" % Test
  )
}
