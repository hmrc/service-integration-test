service-integration-test
========
[ ![Download](https://api.bintray.com/packages/hmrc/releases/service-integration-test/images/download.svg) ](https://bintray.com/hmrc/releases/service-integration-test/_latestVersion)

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

## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
