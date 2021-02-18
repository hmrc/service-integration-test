/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.integration

import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTimeUtils, DateTimeZone}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, SuiteMixin, TestSuite}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.{Application, Environment, Logger, Mode}
import uk.gov.hmrc.integration.servicemanager.ServiceManagerClient

trait ServiceSpec
  extends SuiteMixin
     with BeforeAndAfterAll
     with ScalaFutures
     with IntegrationPatience
     with GuiceOneServerPerSuite {
  this: TestSuite =>

  private val logger = Logger(getClass)

  override def fakeApplication(): Application = {
    logger.info(s"""Starting application with additional config:
      |  ${configMap.mkString("\n  ")}
      |and module overrides:
      |  ${additionalOverrides.mkString("[", "\n", "]")}""".stripMargin)
    // If applicationMode is not set, use Mode.Test (the default for GuiceApplicationBuilder)
    GuiceApplicationBuilder(environment = Environment.simple(mode = applicationMode.getOrElse(Mode.Test)))
      .configure(configMap)
      .overrides(additionalOverrides :_*)
      .build()
  }

  import uk.gov.hmrc.integration.UrlHelper._

  def externalServices: Seq[String]

  def additionalConfig: Map[String, _ <: Any] =
    Map.empty

  def additionalOverrides: Seq[GuiceableModule] =
    Seq.empty

  def testName: String =
    getClass.getSimpleName

  // If applicationMode is set, default to Mode.Dev, to preserve earlier behaviour
  def applicationMode: Option[Mode] =
    Some(Mode.Dev)

  protected val testId =
    TestId(testName)

  protected lazy val externalServicePorts: Map[String, Int] =
    ServiceManagerClient.start(testId, externalServices)

  /** Can be overridden or read to synchronise with mongo testing traits if
    * you require interrogating/mutating mongo data as part of your test.
    */
  // This is not called mongoUri to avoid conflicts with mongo testing traits.
  protected def serviceMongoUri =
    s"mongodb://localhost:27017/${testId.toString}"

  private lazy val mongoConfig =
    Map(s"mongodb.uri" -> serviceMongoUri)

  private lazy val configMap =
    externalServicePorts.foldLeft(Map.empty[String, Any])((map, servicePort) =>
      map ++ (servicePort match {
               case (serviceName, p) =>
                 Map(
                   s"microservice.services.$serviceName.port" -> p,
                   s"microservice.services.$serviceName.host" -> "localhost"
                 )
             })
    ) ++
    mongoConfig ++
    additionalConfig

  def resource(path: String): String =
    s"http://localhost:$port/${-/(path)}"

  def externalResource(serviceName: String, path: String): String = {
    val port =
      externalServicePorts.getOrElse(serviceName, throw new IllegalArgumentException(s"Unknown service '$serviceName'"))
    s"http://localhost:$port/${-/(path)}"
  }

  override def beforeAll() {
    super.beforeAll()
    logger.debug(s"Starting all external services")
    externalServicePorts
  }

  override def afterAll() {
    logger.debug(s"Stopping all external services")
    try {
      ServiceManagerClient.stop(testId, dropDatabases = true)
    } catch {
      case t: Throwable => logger.error(s"An exception occurred while stopping external services", t)
    }
    super.afterAll()
  }
}

object UrlHelper {
  def -/(uri: String) =
    if (uri.startsWith("/")) uri.drop(1) else uri
}

case class TestId(testName: String) {

  val runId =
    DateTimeFormat
      .forPattern("HHmmssSSS")
      .withZone(DateTimeZone.forID("Europe/London"))
      .print(DateTimeUtils.currentTimeMillis())

  override val toString =
    s"${testName.toLowerCase.take(30)}-$runId"
}
