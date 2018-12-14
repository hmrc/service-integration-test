import uk.gov.hmrc.SbtAutoBuildPlugin

enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)

name := "service-integration-test"
majorVersion := 0
makePublicallyAvailableOnBintray := true
libraryDependencies ++= compileDependencies ++ testDependencies

resolvers := Seq(
  Resolver.bintrayRepo("hmrc", "releases"),
  Resolver.typesafeRepo("releases")
)

val compileDependencies = Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % "provided"
)

val testDependencies = Seq(
  "org.pegdown" % "pegdown" % "1.6.0" % "test"
)
