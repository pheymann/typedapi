import sbt.Keys._

val `compiler-2.12` = Seq(
  "-deprecation",
  "-encoding", "utf-8",
  "-explaintypes",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:option-implicit",
  "-Xlint:type-parameter-shadow",
  "-Xlint:unsound-match",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-numeric-widen",
  //   "-Ywarn-unused:implicits", -> get errors for implicit evidence
  "-Ywarn-unused:imports",
  //   "-Ywarn-unused:locals",
  "-Ywarn-unused:privates"
)

val `compiler-2.11` = Seq(
  "-deprecation",
  "-encoding", "utf-8",
  "-explaintypes",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xfuture",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:option-implicit",
  "-Xlint:type-parameter-shadow",
  "-Xlint:unsound-match",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any"
)

lazy val commonSettings = Seq(
  organization  := "com.github.pheymann",
  version       := "0.2.0",
  crossScalaVersions := Seq("2.11.11", "2.12.4"),
  scalaVersion       := "2.12.4",
  scalacOptions      ++= { CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) => `compiler-2.12`
    case Some((2, 11)) => `compiler-2.11`
    case _             => Seq.empty[String]
  }},
  publishTo := sonatypePublishTo.value
)

lazy val mavenSettings = Seq(
  sonatypeProfileName := "pheymann",
  publishMavenStyle   := true,
  pomExtra in Global  := {
    <url>https://github.com/pheymann/typedapi</url>
      <licenses>
        <license>
          <name>MIT</name>
          <url>https://github.com/pheymann/typedapi/blob/master/LICENSE</url>
        </license>
      </licenses>
      <scm>
        <connection>scm:git:github.com/pheymann/typedapi</connection>
        <developerConnection>scm:git:git@github.com:pheymann/typedapi</developerConnection>
        <url>github.com/pheymann/typedapi</url>
      </scm>
      <developers>
        <developer>
          <id>pheymann</id>
          <name>Paul Heymann</name>
          <url>https://github.com/pheymann</url>
        </developer>
      </developers>
  }
)

lazy val typedapi = project
  .in(file("."))
  .settings(commonSettings: _*)
  .aggregate(
    `shared-js`, 
    `shared-jvm`, 
    `client-js`, 
    `client-jvm`, 
    server, 
    `http4s-client`, 
    `http4s-server`, 
    `akka-http-client`,
    `akka-http-server`,
    `js-client`,
    `scalaj-http-client`,
    `http-support-tests`,
    `ammonite-client-support`
  )

lazy val shared = crossProject.crossType(CrossType.Pure)
  .in(file("shared"))
  .settings(
    commonSettings,
    mavenSettings,
    name := "typedapi-shared",
    libraryDependencies ++= Dependencies.shared
  )

lazy val `shared-js` = shared.js
lazy val `shared-jvm` = shared.jvm

lazy val client = crossProject.crossType(CrossType.Pure)
  .in(file("client"))
  .settings(
    commonSettings,
    mavenSettings,
    name := "typedapi-client",
    libraryDependencies ++= Dependencies.client
  )
  .dependsOn(shared)

lazy val `client-js` = client.js
lazy val `client-jvm` = client.jvm

lazy val server = project
  .in(file("server"))
  .settings(
    commonSettings,
    mavenSettings,
    name := "typedapi-server",
    libraryDependencies ++= Dependencies.server
  )
  .dependsOn(`shared-jvm`)


lazy val `http4s-client` = project
  .in(file("http4s-client"))
  .settings(
    commonSettings,
    mavenSettings,
    name := "typedapi-http4s-client",
    libraryDependencies ++= Dependencies.http4sClient,
  )
  .dependsOn(`client-jvm`)

lazy val `http4s-server` = project
  .in(file("http4s-server"))
  .settings(
    commonSettings,
    mavenSettings,
    name := "typedapi-http4s-server",
    libraryDependencies ++= Dependencies.http4sServer,
  )
  .dependsOn(server)

lazy val `akka-http-client` = project
  .in(file("akka-http-client"))
  .settings(
    commonSettings,
    mavenSettings,
    name := "typedapi-akka-http-client",
    libraryDependencies ++= Dependencies.akkaHttpClient
  )
  .dependsOn(`client-jvm`)

lazy val `akka-http-server` = project
  .in(file("akka-http-server"))
  .settings(
    commonSettings,
    mavenSettings,
    name := "typedapi-akka-http-server",
    libraryDependencies ++= Dependencies.akkaHttpServer
  )
  .dependsOn(server)

lazy val `js-client` = project
  .in(file("js-client"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    commonSettings,
    mavenSettings,
    name := "typedapi-js-client",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.6" % Compile
    )
  )
  .dependsOn(`client-js`)

lazy val `scalaj-http-client` = project
  .in(file("scalaj-http-client"))
  .settings(
    commonSettings,
    mavenSettings,
    name := "typedapi-scalaj-http-client",
    libraryDependencies ++= Dependencies.scalajHttpClient
  )
  .dependsOn(`client-jvm`)

lazy val `http-support-tests` = project
  .in(file("http-support-tests"))
  .settings(
    commonSettings,
    parallelExecution in Test := false,
    libraryDependencies ++= Dependencies.httpSupportTests
  )
  .dependsOn(`http4s-client`, `http4s-server`, `akka-http-client`, `akka-http-server`, `scalaj-http-client`)

lazy val `ammonite-client-support` = project
  .in(file("ammonite-client-support"))
  .settings(
    commonSettings,
    mavenSettings,
    name := "typedapi-ammonite-client",
    libraryDependencies ++= Dependencies.ammoniteSupport
  )
  .dependsOn(`scalaj-http-client`)
