package typedapi.server.http4s

import org.http4s.{Headers, Header, Request, Uri}
import org.http4s.dsl.io._
import org.http4s.client.blaze._
import org.http4s.client.dsl.io._
import org.http4s.server.blaze.BlazeBuilder
import cats.effect.IO
import org.specs2.mutable.Specification

final class ServerApiSpec extends Specification {

  sequential

  import TestServer._

  val client = Http1Client[IO]().unsafeRunSync

  def tests(port: Int) = {
    "paths and segments" >> {
        client.expect[User](s"http://localhost:$port/path").unsafeRunSync() === User("joe", 27)
        client.expect[User](s"http://localhost:$port/segment/jim").unsafeRunSync() === User("jim", 27)
      }
  
      "queries" >> {
        client.expect[User](s"http://localhost:$port/query?age=42").unsafeRunSync() === User("joe", 42)
      }
 
      "headers" >> {
        client.expect[User](Request[IO](method = GET, uri = Uri.fromString(s"http://localhost:$port/header").getOrElse(null), headers = Headers(Header("age", "42")))).unsafeRunSync() === User("joe", 42)
        client.expect[User](Request[IO](method = GET, uri =Uri.fromString(s"http://localhost:$port/header/raw").getOrElse(null), headers = Headers(Header("age", "42"), Header("name", "jim")))).unsafeRunSync() === User("jim", 42)
      }

      "methods" >> {
        client.expect[User](s"http://localhost:$port/").unsafeRunSync() === User("joe", 27)
        client.expect[User](PUT(Uri.fromString(s"http://localhost:$port/").getOrElse(null))).unsafeRunSync() === User("joe", 27)
        client.expect[User](PUT(Uri.fromString(s"http://localhost:$port/body").getOrElse(null), User("joe", 27))).unsafeRunSync() === User("joe", 27)
        client.expect[User](POST(Uri.fromString(s"http://localhost:$port/").getOrElse(null))).unsafeRunSync() === User("joe", 27)
        client.expect[User](POST(Uri.fromString(s"http://localhost:$port/body").getOrElse(null), User("joe", 27))).unsafeRunSync() === User("joe", 27)
        client.expect[User](DELETE(Uri.fromString(s"http://localhost:$port/?reasons=because").getOrElse(null))).unsafeRunSync() === User("joe", 27)
      }
  }

  val serverDsl = startDsl(BlazeBuilder[IO]).unsafeRunSync()
  val serverDef = startDef(BlazeBuilder[IO]).unsafeRunSync()

  s"http4s implements TypedApi's server interface" >> {
    "api dsl" >> {
      tests(9000)
    }

    "api definition" >> {
      tests(10000)
    }

    step {
      serverDsl.shutdown.unsafeRunSync()
      serverDef.shutdown.unsafeRunSync()
    }
  }
}
