package typedapi.client.http4s

import typedapi.client._

import cats.effect.IO
import org.http4s.client.blaze.Http1Client
import org.specs2.mutable.Specification

final class ApiRequestSpec extends Specification {

  sequential

  val server = TestServer.start()

  val cm = ClientManager(Http1Client[IO]().unsafeRunSync, "http://localhost", 9001)

  import TestServer.{decoder, encoder}

  "http4s client" >> {
    "api dsl" >> {
      import typedapi.dsl._

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
    }

    "api definition" >> {
      import typedapi._

      "paths and segments" >> {
        val a = derive(api(Get[User], Root / "path"))
        a().run[IO](cm).unsafeRunSync() === User("foo", 27)
        
        val b = derive(api(Get[User], Root / "segment" / Segment[String]('name)))
        b("jim").run[IO](cm).unsafeRunSync() === User("jim", 27)
      }
  
      "queries" >> {
        val a = derive(api(Get[User], Root / "query", Queries add Query[Int]('age)))
        a(42).run[IO](cm).unsafeRunSync() === User("foo", 42)
      }
 
      "headers" >> {
        val a = derive(api(Get[User], Root / "header", headers = Headers add Header[Int]('age)))
        a(42).run[IO](cm).unsafeRunSync() === User("foo", 42)

        val b = derive(api(Get[User], Root / "header" / "raw", headers = Headers add Header[Int]('age) add RawHeaders))
        b(42, Map("name" -> "jim")).run[IO](cm).unsafeRunSync() === User("jim", 42)
      }

      "methods" >> {
        val a = derive(api(Get[User]))
        a().run[IO](cm).unsafeRunSync() === User("foo", 27)
        val b = derive(api(Put[User]))
        b().run[IO](cm).unsafeRunSync() === User("foo", 27)
        val c = derive(apiWithBody(Put[User], ReqBody[User], Root / "body"))
        c(User("jim", 42)).run[IO](cm).unsafeRunSync() === User("jim", 42)
        val d = derive(api(Post[User]))
        d().run[IO](cm).unsafeRunSync() === User("foo", 27)
        val e = derive(apiWithBody(Post[User], ReqBody[User], Root / "body"))
        e(User("jim", 42)).run[IO](cm).unsafeRunSync() === User("jim", 42)
        val f = derive(api(Delete[User], Root, Queries add Query[List[String]]('reasons)))
        f(List("because")).run[IO](cm).unsafeRunSync() === User("foo", 27)
      }
    }

    "composed api" >> {
      import typedapi.dsl._

      val (a, b) = deriveAll(
        (:= :> "path" :> Get[User]) :|:
        (:= :> "segment" :> Segment[String]('name) :> Get[User])
      )

      a().run[IO](cm).unsafeRunSync() === User("foo", 27)
      b("jim").run[IO](cm).unsafeRunSync() === User("jim", 27)
    }

    step {
      server.shutdown.unsafeRunSync()
    }
  }
}
