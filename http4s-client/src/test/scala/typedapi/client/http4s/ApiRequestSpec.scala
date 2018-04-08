package typedapi.client.http4s

import typedapi.client._

import cats.effect.IO
import org.http4s.client.blaze.Http1Client
import org.specs2.mutable.Specification

final class ApiRequestSpec extends Specification {

  sequential

  val server = TestServer.start()

  implicit val cm = ClientManager(Http1Client[IO]().unsafeRunSync, "http://localhost", 9001)

  import TestServer.{decoder, encoder}

  "http4s client" >> {
    "api dsl" >> {
      import typedapi.dsl._

      "paths and segments" >> {
        val a = compile(:= :> "path" :> Get[User])
        a().run[IO].unsafeRunSync() === User("foo", 27)
        
        val b = compile(:= :> "segment" :> Segment[String]('name) :> Get[User])
        b("jim").run[IO].unsafeRunSync() === User("jim", 27)
      }
  
      "queries" >> {
        val a = compile(:= :> "query" :> Query[Int]('age) :> Get[User])
        a(42).run[IO].unsafeRunSync() === User("foo", 42)
      }
 
      "headers" >> {
        val a = compile(:= :> "header" :> Header[Int]('age) :> Get[User])
        a(42).run[IO].unsafeRunSync() === User("foo", 42)

        val b = compile(:= :> "header" :> "raw" :> Header[Int]('age) :> RawHeaders :> Get[User])
        b(42, Map("name" -> "jim")).run[IO].unsafeRunSync() === User("jim", 42)
      }

      "methods" >> {
        val a = compile(:= :> Get[User])
        a().run[IO].unsafeRunSync() === User("foo", 27)
        val b = compile(:= :> Put[User])
        b().run[IO].unsafeRunSync() === User("foo", 27)
        val c = compile(:= :> "body" :> ReqBody[User] :> Put[User])
        c(User("jim", 42)).run[IO].unsafeRunSync() === User("jim", 42)
        val d = compile(:= :> Post[User])
        d().run[IO].unsafeRunSync() === User("foo", 27)
        val e = compile(:= :> "body" :> ReqBody[User] :> Post[User])
        e(User("jim", 42)).run[IO].unsafeRunSync() === User("jim", 42)
        val f = compile(:= :> Query[List[String]]('reasons) :> Delete[User])
        f(List("because")).run[IO].unsafeRunSync() === User("foo", 27)
      }
    }

    "api definition" >> {
      import typedapi._

      "paths and segments" >> {
        val a = compile(api(Get[User], Root :> "path"))
        a().run[IO].unsafeRunSync() === User("foo", 27)
        
        val b = compile(api(Get[User], Root :> "segment" :> Segment[String]('name)))
        b("jim").run[IO].unsafeRunSync() === User("jim", 27)
      }
  
      "queries" >> {
        val a = compile(api(Get[User], Root :> "query", Queries :> Query[Int]('age)))
        a(42).run[IO].unsafeRunSync() === User("foo", 42)
      }
 
      "headers" >> {
        val a = compile(api(Get[User], Root :> "header", headers = Headers :> Header[Int]('age)))
        a(42).run[IO].unsafeRunSync() === User("foo", 42)

        val b = compile(api(Get[User], Root :> "header" :> "raw", headers = Headers :> Header[Int]('age) :> RawHeaders))
        b(42, Map("name" -> "jim")).run[IO].unsafeRunSync() === User("jim", 42)
      }

      "methods" >> {
        val a = compile(api(Get[User]))
        a().run[IO].unsafeRunSync() === User("foo", 27)
        val b = compile(api(Put[User]))
        b().run[IO].unsafeRunSync() === User("foo", 27)
        val c = compile(apiWithBody(Put[User], ReqBody[User], Root :> "body"))
        c(User("jim", 42)).run[IO].unsafeRunSync() === User("jim", 42)
        val d = compile(api(Post[User]))
        d().run[IO].unsafeRunSync() === User("foo", 27)
        val e = compile(apiWithBody(Post[User], ReqBody[User], Root :> "body"))
        e(User("jim", 42)).run[IO].unsafeRunSync() === User("jim", 42)
        val f = compile(api(Delete[User], Root, Queries :> Query[List[String]]('reasons)))
        f(List("because")).run[IO].unsafeRunSync() === User("foo", 27)
      }
    }

    "composed api" >> {
      import typedapi.dsl._

      val (a :|: b :|: =:) = compile(
        (:= :> "path" :> Get[User]) :|:
        (:= :> "segment" :> Segment[String]('name) :> Get[User])
      )

      a().run[IO].unsafeRunSync() === User("foo", 27)
      b("jim").run[IO].unsafeRunSync() === User("jim", 27)
    }

    step {
      server.shutdown.unsafeRunSync()
    }
  }
}
