package http.support.tests.client

import http.support.tests.{User, Api}
import typedapi.client._
import typedapi.client.akkahttp._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.specs2.mutable.Specification
import org.specs2.concurrent.ExecutionEnv

import scala.concurrent.duration._
import scala.concurrent.{Future, Await}

final class AkkaHttpClientSupportSpec(implicit ee: ExecutionEnv) extends Specification {

  import FailFastCirceSupport._

  sequential

  implicit val timeout = 1.second
  implicit val system  = ActorSystem("akka-http-client-spec")
  implicit val mat     = ActorMaterializer()

  import system.dispatcher

  val cm     = ClientManager(Http(), "http://localhost", 9001)
  val server = TestServer.start()

  "akka http client support" >> {
    val (p, s, q, h0, h1, m0, m1, m2, m3, m4, m5) = deriveAll(Api)

    "paths and segments" >> {
      p().run[Future](cm) must beEqualTo(User("foo", 27)).awaitFor(timeout)
      s("jim").run[Future](cm) must beEqualTo( User("jim", 27)).awaitFor(timeout)
    }
    
    "queries" >> {
      q(42).run[Future](cm) must beEqualTo( User("foo", 42)).awaitFor(timeout)
    }
    
    "headers" >> {
      h0(42).run[Future](cm) must beEqualTo( User("foo", 42)).awaitFor(timeout)
      h1(42, Map("name" -> "jim")).run[Future](cm) must beEqualTo( User("jim", 42)).awaitFor(timeout)
    }

    "methods" >> {
      m0().run[Future](cm) must beEqualTo( User("foo", 27)).awaitFor(timeout)
      m1().run[Future](cm) must beEqualTo( User("foo", 27)).awaitFor(timeout)
      m2(User("jim", 42)).run[Future](cm) must beEqualTo( User("jim", 42)).awaitFor(timeout)
      m3().run[Future](cm) must beEqualTo( User("foo", 27)).awaitFor(timeout)
      m4(User("jim", 42)).run[Future](cm) must beEqualTo( User("jim", 42)).awaitFor(timeout)
      m5(List("because")).run[Future](cm) must beEqualTo( User("foo", 27)).awaitFor(timeout)
    }

    step {
      server.shutdown.unsafeRunSync()
      Await.ready(system.terminate, timeout)
    }
  }
}
