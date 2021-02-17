import uk.gov.hmrc.SbtAutoBuildPlugin

lazy val app = Project("service-integration-test", file("."))
  .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
  .settings(
    makePublicallyAvailableOnBintray := true,
    majorVersion := 1,
    libraryDependencies ++= compile ++ test,
    scalaVersion := "2.12.8"
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
  play26 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0"),
  play27 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "4.0.3"),
  play28 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0")
)

val test: Seq[ModuleID] = PlayCrossCompilation.dependencies(
  play26 = Seq("org.pegdown"          % "pegdown"      % "1.6.0"   % Test),
  play27 = Seq("org.pegdown"          % "pegdown"      % "1.6.0"   % Test),
  play28 = Seq("com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % Test)
)
