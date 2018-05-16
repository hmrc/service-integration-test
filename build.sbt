
import uk.gov.hmrc.DefaultBuildSettings.targetJvm
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning.majorVersion


enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)

name                      := "service-integration-test"
majorVersion              := 0
libraryDependencies       ++= compileDependencies ++ testDependencies

resolvers := Seq(
  Resolver.bintrayRepo("hmrc", "releases"),
  Resolver.typesafeRepo("releases")
)

val compileDependencies = Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0"  % "provided"
)

val testDependencies = Seq(
  "org.scalatest"          %% "scalatest"          % "3.0.3"    % "test",
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0"    % "test",
  "org.mockito"            %  "mockito-all"        % "1.9.5"    % "test",
  "org.pegdown"            %  "pegdown"            % "1.6.0"    % "test"
)