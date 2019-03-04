import uk.gov.hmrc.SbtAutoBuildPlugin

lazy val app = Project("service-integration-test", file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    makePublicallyAvailableOnBintray := true,
    majorVersion := 0,
    libraryDependencies ++= compile ++ test,
    resolvers := Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.typesafeRepo("releases")),
    crossScalaVersions := List("2.11.12", "2.12.8")
  )
  .settings(PlayCrossCompilation.playCrossCompilationSettings)

val compile: Seq[ModuleID] = PlayCrossCompilation.dependencies(
  shared = Seq(
    // force dependencies due to security flaws found in jackson-databind < 2.9.x using XRay
    "com.fasterxml.jackson.core"     % "jackson-core"            % "2.9.7",
    "com.fasterxml.jackson.core"     % "jackson-databind"        % "2.9.7",
    "com.fasterxml.jackson.core"     % "jackson-annotations"     % "2.9.7",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8"   % "2.9.7",
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.9.7"
  ),
  play25 = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % "provided",
    // force dependencies due to security flaws found in xercesImpl 2.11.0
    // only applies to play 2.5 since it was removed from play 2.6
    // https://github.com/playframework/playframework/blob/master/documentation/manual/releases/release26/migration26/Migration26.md#xercesimpl-removal
    "xerces" % "xercesImpl" % "2.12.0"
  ),
  play26 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % "provided")
)

val test: Seq[ModuleID] = PlayCrossCompilation.dependencies(
  shared = Seq("org.pegdown"            % "pegdown"             % "1.6.0" % Test),
  play25 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test),
  play26 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test)
)
