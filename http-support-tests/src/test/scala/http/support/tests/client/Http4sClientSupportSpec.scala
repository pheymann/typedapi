package http.support.tests.client

import http.support.tests.{UserCoding, User, Api}
import typedapi.client._
import typedapi.client.http4s._
import cats.effect.IO
import org.http4s.client.blaze.Http1Client
import org.specs2.mutable.Specification

final class Http4sClientSupportSpec extends Specification {

  import UserCoding._

  sequential

  val cm = ClientManager(Http1Client[IO]().unsafeRunSync, "http://localhost", 9001)

  val server = TestServer.start()

  "http4s client support" >> {
    val (p, s, q, header, fixed, clInH, clFixH, serMatchH, serSendH, m0, m1, m2, m3, m4, m5, _, _, _) = deriveAll(Api)

    "paths and segments" >> {
      p().run[IO](cm).unsafeRunSync() === User("foo", 27)
      s("jim").run[IO](cm).unsafeRunSync() === User("jim", 27)
    }
    
    "queries" >> {
      q(42).run[IO](cm).unsafeRunSync() === User("foo", 42)
    }
    
    "headers" >> {
      header(42).run[IO](cm).unsafeRunSync() === User("foo", 42)
      fixed().run[IO](cm).unsafeRunSync() === User("joe", 27)
      clInH("jim").run[IO](cm).unsafeRunSync === User("jim", 27)
      clFixH().run[IO](cm).unsafeRunSync() === User("joe", 27)
      serMatchH().run[IO](cm).unsafeRunSync() === User("joe", 27)
      serSendH().run[IO](cm).unsafeRunSync() === User("joe", 27)
    }

    "methods" >> {
      m0().run[IO](cm).unsafeRunSync() === User("foo", 27)
      m1().run[IO](cm).unsafeRunSync() === User("foo", 27)
      m2(User("jim", 42)).run[IO](cm).unsafeRunSync() === User("jim", 42)
      m3().run[IO](cm).unsafeRunSync() === User("foo", 27)
      m4(User("jim", 42)).run[IO](cm).unsafeRunSync() === User("jim", 42)
      m5(List("because")).run[IO](cm).unsafeRunSync() === User("foo", 27)
    }

    step {
      server.shutdown.unsafeRunSync()
    }
  }
}
