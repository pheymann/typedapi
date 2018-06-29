package http.support.tests.client

import http.support.tests.{UserCoding, User}
import typedapi.client._
import typedapi.dsl._
import typedapi.client.http4s._
import cats.effect.IO
import org.http4s.client.blaze.Http1Client
import org.specs2.mutable.Specification

final class Http4sClientSupportSpec extends Specification {

  sequential

  val cm = ClientManager(Http1Client[IO]().unsafeRunSync, "http://localhost", 9001)

  val server = TestServer.start()

  "http4s client support" >> {
    import UserCoding._

    "paths and segments" >> {
      val a = derive(:= :> "path" :> Get[User])
      a().run[IO](cm).unsafeRunSync() === User("foo", 27)
      
      val b = derive(:= :> "segment" :> Segment[String]('name) :> Get[User])
      b("jim").run[IO](cm).unsafeRunSync() === User("jim", 27)
    }
    
    "queries" >> {
      val a = derive(:= :> "query" :> Query[Int]('age) :> Get[User])
      a(42).run[IO](cm).unsafeRunSync() === User("foo", 42)
    }
    
    "headers" >> {
      val a = derive(:= :> "header" :> Header[Int]('age) :> Get[User])
      a(42).run[IO](cm).unsafeRunSync() === User("foo", 42)

      val b = derive(:= :> "header" :> "raw" :> Header[Int]('age) :> RawHeaders :> Get[User])
      b(42, Map("name" -> "jim")).run[IO](cm).unsafeRunSync() === User("jim", 42)
    }

    "methods" >> {
      val a = derive(:= :> Get[User])
      a().run[IO](cm).unsafeRunSync() === User("foo", 27)
      val b = derive(:= :> Put[User])
      b().run[IO](cm).unsafeRunSync() === User("foo", 27)
      val c = derive(:= :> "body" :> ReqBody[User] :> Put[User])
      c(User("jim", 42)).run[IO](cm).unsafeRunSync() === User("jim", 42)
      val d = derive(:= :> Post[User])
      d().run[IO](cm).unsafeRunSync() === User("foo", 27)
      val e = derive(:= :> "body" :> ReqBody[User] :> Post[User])
      e(User("jim", 42)).run[IO](cm).unsafeRunSync() === User("jim", 42)
      val f = derive(:= :> Query[List[String]]('reasons) :> Delete[User])
      f(List("because")).run[IO](cm).unsafeRunSync() === User("foo", 27)
    }

    step {
      server.shutdown.unsafeRunSync()
    }
  }
}
