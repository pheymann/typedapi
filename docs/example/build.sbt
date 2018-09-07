
val typedapiVersion = "0.2.0"
val http4sVersion   = "0.18.0"

val commonSettings = Seq(
  scalaVersion := "2.12.4"
)

lazy val root = project
  .in(file("."))
  .aggregate(`shared-jvm`, `shared-js`, `client-jvm`, `client-js`, server)

lazy val shared = crossProject.crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.github.pheymann" %%% "typedapi-shared" % typedapiVersion,

      "io.circe" %%% "circe-core" % "0.9.1",
      "io.circe" %%% "circe-parser" % "0.9.1",
      "io.circe" %%% "circe-generic" % "0.9.1"
    )
  )

lazy val `shared-js` = shared.js
lazy val `shared-jvm` = shared.jvm

lazy val server = project
  .in(file("server"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.github.pheymann" %% "typedapi-http4s-server" % typedapiVersion,

      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion
    )
  )
  .dependsOn(`shared-jvm`)

lazy val `client-jvm` = project
  .in(file("client-jvm"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.github.pheymann" %% "typedapi-http4s-client" % typedapiVersion,

      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion
    )
  )
  .dependsOn(`shared-jvm`)

lazy val `client-js` = project
  .in(file("client-js"))
  .enablePlugins(ScalaJSPlugin)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.github.pheymann" %%% "typedapi-js-client" % typedapiVersion,
    ),
    scalaJSUseMainModuleInitializer := true
  )
  .dependsOn(`shared-js`)
