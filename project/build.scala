import sbt._

object Dependencies {

  private val specs2V = "3.9.4"

  val shared = Seq(
    "com.chuusai" %% "shapeless" % "2.3.3" % Compile,

    "org.specs2"  %% "specs2-core" % specs2V % Test
  )

  val client = Seq(
    "org.specs2"  %% "specs2-core" % specs2V % Test
  )

  val server = Seq(
    "org.specs2"  %% "specs2-core" % specs2V % Test
  )

  private val http4sV = "0.18.0"

  val http4sClient = Seq(
    "org.http4s" %% "http4s-blaze-client" % http4sV % Provided,
  )

  val http4sServer = Seq(
    "org.http4s" %% "http4s-blaze-server" % http4sV % Provided,
    "org.http4s" %% "http4s-dsl" % http4sV % Provided
  )

  private val akkaHttpV = "10.0.13"

  val akkaHttpClient = Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpV % Provided,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpV % Provided
  )

  val akkaHttpServer = Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpV % Provided,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpV % Provided
  )

  private val scalajHttpV = "2.4.1"

  val scalajHttpClient = Seq(
    "org.scalaj" %% "scalaj-http" % scalajHttpV % Provided
  )

  private val circeV = "0.9.1"

  val httpSupportTests = Seq(
    "org.specs2" %% "specs2-core" % specs2V % Test,

    "org.http4s" %% "http4s-blaze-client" % http4sV % Test,
    "org.http4s" %% "http4s-blaze-server" % http4sV % Test,
    "org.http4s" %% "http4s-dsl" % http4sV          % Test,
    "org.http4s" %% "http4s-circe" % http4sV        % Test,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV  % Test,
    "org.scalaj" %% "scalaj-http" % scalajHttpV     % Test,

    "io.circe"   %% "circe-core" % circeV               % Test,
    "io.circe"   %% "circe-parser" % circeV             % Test,
    "io.circe"   %% "circe-generic" % circeV            % Test,
    "de.heikoseeberger" %% "akka-http-circe" % "1.21.0" % Test,

    "org.specs2" %% "specs2-core" % specs2V         % Test
  )

  val ammoniteSupport = Seq(
    "org.scalaj" %% "scalaj-http" % scalajHttpV % Compile
  )
}
