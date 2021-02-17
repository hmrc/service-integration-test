/**
  * Created by sammiele on 21/12/2018.
  */

import uk.gov.hmrc.playcrosscompilation.{AbstractPlayCrossCompilation, PlayVersion}

object PlayCrossCompilation extends AbstractPlayCrossCompilation(defaultPlayVersion = PlayVersion.Play28) {
  override lazy val playDir = playVersion match {
    case PlayVersion.Play26 => "play-26"
    case PlayVersion.Play27 => "play-26"
    case PlayVersion.Play28 => "play-26"
  }
}
