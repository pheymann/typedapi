package typedapi.server.http4s

import org.http4s.client.UnexpectedStatus
import org.http4s.dsl.io._
import org.http4s.client.blaze._
import org.http4s.client.dsl.io._
import org.http4s.server.blaze.BlazeBuilder
import cats.effect.IO
import org.specs2.mutable.Specification

final class ServerApiSpec extends Specification {

  import TestServer._

  val client = Http1Client[IO]().unsafeRunSync
  val server = start(BlazeBuilder[IO]).unsafeRunSync()

  "http4s implements TypedApi's server interface" >> {
    "with request body, without request body and raw response" >> {
      client.expect[User](PUT(uri("http://localhost:9000/with/reqbody"), User("joe", 27))).unsafeRunSync() === User("joe", 27)
      client.expect[User]("http://localhost:9000/without/reqbody").unsafeRunSync() === User("joe", 27)
      client.expect[String]("http://localhost:9000/raw").unsafeRunSync() === "hello"
    }

    "not found" >> {
      client.expect[String]("http://localhost:9000/wrong").unsafeRunSync() must throwA(UnexpectedStatus(NotFound))
    }

    "bad request" >> {
      client.expect[String]("http://localhost:9000/query?query=hello").unsafeRunSync() must throwA(UnexpectedStatus(BadRequest))
      client.expect[String]("http://localhost:9000/query?wrong=hello").unsafeRunSync() must throwA(UnexpectedStatus(BadRequest))
    }

    step {
      server.shutdown.unsafeRunSync()
    }
  }
}
