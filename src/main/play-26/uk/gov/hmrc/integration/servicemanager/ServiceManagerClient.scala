/*
 * Copyright 2018 HM Revenue & Customs
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

import java.util.concurrent.TimeUnit

import play.api.libs.json.{JsPath, Json, JsonValidationError}
import play.api.libs.ws.WSResponse
import play.api.libs.ws.ahc.{AhcWSClient, AhcWSClientConfig, StandaloneAhcWSClient}

import scala.concurrent.duration._

class ServiceManagerClient extends ServiceManagerCommon {

  lazy val client = new AhcWSClient(StandaloneAhcWSClient.apply(AhcWSClientConfig.apply()))

  override def setupExtendedTimeoutClient(timeout: Duration): AhcWSClient = {
   new AhcWSClient(
    StandaloneAhcWSClient.apply(AhcWSClientConfig.apply(idleConnectionInPoolTimeout = Duration.apply(timeout.toMillis, TimeUnit.MILLISECONDS)))
    )
  }

  override def handleResp(response: WSResponse)= {
    val servicePorts: Seq[ServiceManagementResponse] = response.json
        .validate[Seq[ServiceManagementResponse]]
          .fold(
            errs =>
              throw new JsException(
                    "POST",
                    serviceManagerStartUrl,
                    response.body,
                    classOf[Seq[ServiceManagementResponse]],
                    errs),
                valid => valid)

            servicePorts.map(s => s.serviceName -> s.port).toMap
    }

  override def handleVersionVaribleResponse(service: String, response: WSResponse): VersionEnvironmentVariable = {
    response.json
      .validate[VersionEnvironmentVariable]
      .fold(
        errors =>
          throw new JsException(
            "GET",
            s"$serviceManagerVersionVariableUrl?service=$service",
            response.body,
            classOf[VersionEnvironmentVariable],
            errors),
        valid => valid
      )
  }
}

class JsException(
  method: String,
  url: String,
  body: String,
  clazz: Class[_],
  errors: Seq[(JsPath, Seq[JsonValidationError])])
    extends Exception {
  override def getMessage: String =
    s"$method of '$url' returned invalid json. Attempting to convert to ${clazz.getName} gave errors: $errors"
}
