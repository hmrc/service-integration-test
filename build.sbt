import uk.gov.hmrc.SbtAutoBuildPlugin


lazy val app = Project("service-integration-test", file("."))
.enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
.settings(
  makePublicallyAvailableOnBintray := true,
  majorVersion                     := 0,
  libraryDependencies ++= compile ++ test,
  resolvers := Seq( Resolver.bintrayRepo("hmrc", "releases"), Resolver.typesafeRepo("releases") )
)
.settings(PlayCrossCompilation.playCrossCompilationSettings)

val compile: Seq[ModuleID] = PlayCrossCompilation.dependencies(
  play25 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % "provided"),
  play26 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % "provided")
)

val test: Seq[ModuleID] = PlayCrossCompilation.dependencies(
  shared = Seq("org.pegdown"            % "pegdown"             % "1.6.0" % Test),
  play25 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test),
  play26 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test)
)

