/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.integration.servicemanager

import akka.stream.Materializer
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.duration.{Duration, _}

object AhcWsClientFactory {
  def createClient(pooledConnectionIdleTimeout: Duration = 1.minute)(implicit mat: Materializer): AhcWSClient =
    new AhcWSClient({
      val builder = new DefaultAsyncHttpClientConfig.Builder()
      builder.setPooledConnectionIdleTimeout(pooledConnectionIdleTimeout.toMillis.toInt)
      builder.build()
    })
}
