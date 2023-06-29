# service-integration-test

## This library is deprecated.

### It relied on the deprecated [service-manager](https://github.com/hmrc/service-manager).

### Instead, [service-manager-2](https://github.com/hmrc/sm2) should be started with an appropriate PROFILE before running the integration tests.

------

service-integration-test is a Scala library providing some useful functionality for integration tests.

The library starts all your dependencies using [smserver](https://github.com/hmrc/service-manager) and then runs the tests.

## Adding to your service

Include the following dependency in your SBT build

```scala
resolvers += Resolver.bintrayRepo("hmrc", "releases")

libraryDependencies += "uk.gov.hmrc" %% "service-integration-test" % "x.x.x" % "test,it"
```

## How to use

In your scalatest Spec extend from `ServiceSpec` and provide a list of external services you need smserver to start.

In this example, a call is made to the /example/hello-world endpoint on the tested microservice.
This service has a dependency to `a-microservice` that is started on beforeAll and stopped on afterAll:

```scala
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.integration.ServiceSpec

class ExampleIntegrationTest extends WordSpec with Matchers with ServiceSpec  {

  def externalServices: Seq[String] = Seq("a-microservice")

  "This integration test" should {
    "start a-microservice via smserver" in {

      val wsClient = app.injector.instanceOf[WSClient]
      val response = wsClient.url(resource("/example/hello-world")).get.futureValue
      response.status shouldBe 200

    }
  }
}
```

## How to run

Run service-manager
```bash
smserver
```

Then run tests.


## CHANGES

### 1.3.0

- Drops Play 2.6 and 2.7 support - only supports Play 2.8
- Cross builds for Scala 2.12 and 2.13

### 1.0.0

Run mode prefix is dropped from `microservice.services.` configuration - it is expected that your services do not require the run mode prefix anymore.

Mongodb uri is now configured with `mongodb.uri` (previously `${runModePrefix}microservice.mongodb.uri`).

The mongodb uri is configured with `serviceMongoUri`. The default is generated to avoid conflicts, and should suffice; but if you are combining the `ServiceSpec` with a mongo test trait, then you will need to synchronise the two. E.g.
```scala
class ExampleIntegrationTest extends WordSpec with Matchers with ServiceSpec with MongoSupport {
  override def mongoUri = super.serviceMongoUri
  // alternatively
  //override def serviceMongoUri = super.mongoUri
}
```


## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
