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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import play.api.libs.ws.WSResponse
import play.api.libs.json.{JsPath, Json}
import play.api.libs.ws.ahc.AhcWSClient
import uk.gov.hmrc.integration.TestId

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.sys.addShutdownHook

object ServiceManagerCommon {

  val client = new ServiceManagerClient()

  def start(testId: TestId, externalServices: Seq[String], timeout: Duration = Duration(60, TimeUnit.SECONDS)): Map[String, Int] = {
    client.start(testId, externalServices, timeout)
  }

  def stop(testId: TestId, dropDatabases: Boolean): Unit = {
    client.stop(testId, dropDatabases)
  }

  def version_variable(service: String) = {
    client.version_variable(service)
  }
}

abstract class ServiceManagerCommon {

  implicit val system = ActorSystem("serviceManagerClient")
  addShutdownHook(system.terminate)
  implicit val mat = ActorMaterializer()

  val client: AhcWSClient
  protected val serviceManagerStartUrl = "http://localhost:8085/start"
  protected val serviceManagerStopUrl = "http://localhost:8085/stop"
  protected val serviceManagerVersionVariableUrl = "http://localhost:8085/version_variable"
  implicit val externalServiceFormat = Json.format[ExternalService]
  implicit val startRequestFormat = Json.format[ServiceManagementStartRequest]
  implicit val stopRequestFormat = Json.format[ServiceManagementStopRequest]
  implicit val responseFormat = Json.format[ServiceManagementResponse]
  implicit val versionEnvironmentVariableFormat = Json.format[VersionEnvironmentVariable]

  def handleResp(response: WSResponse): Map[String, Int]
  def setupExtendedTimeoutClient(duration: Duration): AhcWSClient
  def handleVersionVaribleResponse(service: String, response: WSResponse): VersionEnvironmentVariable

  def start(testId: TestId, externalServices: Seq[String], timeout: Duration = Duration(60, TimeUnit.SECONDS)): Map[String, Int] =
    if (externalServices.isEmpty)
      Map.empty
    else {
      val extendedTimeoutClient = setupExtendedTimeoutClient(timeout)
      val f = extendedTimeoutClient
        .url(serviceManagerStartUrl)
        .withRequestTimeout(timeout)
        .post(
          Json.toJson(ServiceManagementStartRequest(testId.toString, externalServices.map(s => ExternalService(s)))))
        .map { response: WSResponse =>
          if (response.status >= 200 && response.status <= 299) {
            handleResp(response)
          } else {
            throw new RuntimeException(
              s"Received unexpected response from ServiceManager: ${response.status}\n\n" + response.body)
          }
        }
      Await.result(f.andThen { case _ => extendedTimeoutClient.close() }, Duration(5, TimeUnit.MINUTES))
    }

  def stop(testId: TestId, dropDatabases: Boolean) {
    Await.result(
      client
        .url(serviceManagerStopUrl)
        .post(Json.toJson(ServiceManagementStopRequest(testId.toString, dropDatabases))),
      Duration(30, TimeUnit.SECONDS)
    )
  }

  def version_variable(service: String): VersionEnvironmentVariable = {
    val versionEnvironmentVariable: Future[VersionEnvironmentVariable] =
      client.url(serviceManagerVersionVariableUrl).withQueryString("service" -> service).get().map {
        response: WSResponse =>
          handleVersionVaribleResponse(service, response)
      }

    Await.result(versionEnvironmentVariable, Duration(5, TimeUnit.MINUTES))
  }

}


case class ExternalService(serviceName: String,runFrom: String = "RELEASE_JAR", classifier: Option[String] = None, version: Option[String] = None)
case class VersionEnvironmentVariable(variable: String)
case class ServiceManagementStartRequest(testId: String, services: Seq[ExternalService])
case class ServiceManagementStopRequest(testId: String, dropDatabases: Boolean)
case class ServiceManagementResponse(port: Int, serviceName: String)
