import uk.gov.hmrc.SbtAutoBuildPlugin

val scala2_12 = "2.12.15"
val scala2_13 = "2.13.7"

lazy val app = Project("service-integration-test", file("."))
  .settings(
    isPublicArtefact := true,
    majorVersion := 1,
    libraryDependencies ++= compile ++ test,
    scalaVersion := scala2_12,
    crossScalaVersions := Seq(scala2_12, scala2_13)
  )
  .settings(PlayCrossCompilation.playCrossCompilationSettings)

val compile: Seq[ModuleID] = PlayCrossCompilation.dependencies(
  play28 = Seq("org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0")
)

val test: Seq[ModuleID] = PlayCrossCompilation.dependencies(
  play28 = Seq("com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % Test)
)
