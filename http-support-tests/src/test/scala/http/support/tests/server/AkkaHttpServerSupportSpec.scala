package http.support.tests.server

import http.support.tests.{Api, UserCoding}
import typedapi.server._
import typedapi.server.akkahttp._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import cats.implicits._
import org.specs2.concurrent.ExecutionEnv

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._

final class AkkaHttpServerSupportSpec(implicit ee: ExecutionEnv) extends ServerSupportSpec[Future]()(catsStdInstancesForFuture(ee.ec)) {

  import UserCoding._
  import FailFastCirceSupport._

  implicit val timeout = 5.second
  implicit val system  = ActorSystem("akka-http-server-spec", defaultExecutionContext = Some(ee.ec))
  implicit val mat     = ActorMaterializer()

  import system.dispatcher

  val endpoints = deriveAll[Future](Api).from(path, segment, query, header, get, put, putB, post, postB, delete)
  val sm        = ServerManager(Http(), "localhost", 9000)
  val server    = mount(sm, endpoints)

  "akka http implements TypedApi's server interface" >> {
    tests(9000)

    step {
      Await.ready(server.map(_.unbind()), timeout)
      Await.ready(system.terminate, timeout)
    }
  }

}
