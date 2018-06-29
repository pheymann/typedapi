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
  version       := "0.1.0-RC5",
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
  .aggregate(`shared-js`, `shared-jvm`, `client-js`, `client-jvm`, server, `http4s-client-js`, `http4s-client-jvm` , `http4s-server`)

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


lazy val `http4s-client` = crossProject.crossType(CrossType.Pure)
  .in(file("http4s-client"))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    mavenSettings,
    Defaults.itSettings,
    name := "typedapi-http4s-client",
    libraryDependencies ++= Dependencies.http4sClient,

    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )
  .dependsOn(client)

lazy val `http4s-client-js` = `http4s-client`.js
lazy val `http4s-client-jvm` = `http4s-client`.jvm

lazy val `http4s-server` = project
  .in(file("http4s-server"))
  .configs(IntegrationTest)
  .settings(
    commonSettings,
    mavenSettings,
    Defaults.itSettings,
    name := "typedapi-http4s-server",
    libraryDependencies ++= Dependencies.http4sServer,

    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )
  .dependsOn(server)

lazy val `http-support-tests` = project
  .in(file("http-support-tests"))
  .settings(
    commonSettings,
    parallelExecution in Test := false,
    libraryDependencies ++= Dependencies.httpSupportTests,

    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )
  .dependsOn(`http4s-client-jvm`, `http4s-server`)
