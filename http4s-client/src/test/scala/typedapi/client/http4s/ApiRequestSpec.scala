package typedapi.client.http4s

import typedapi.client._

import cats.effect.IO
import org.http4s.client.blaze.Http1Client
import org.specs2.mutable.Specification

final class ApiRequestSpec extends Specification {

  val server = TestServer.start()

  val api = 
    (:= :> "find" :> Segment[String]('name) :> Query[Int]('age) :> Get[User]) :|:
    (:= :> "without" :> "reqbody" :> Put[User]) :|:
    (:= :> "with" :> "reqbody" :> ReqBody[User] :> Put[User]) :|:
    (:= :> "with" :> "reqbody" :> ReqBody[User] :> Post[User]) :|:
    (:= :> "without" :> "reqbody" :> Post[User]) :|:
    (:= :> "delete" :> Segment[String]('name) :> Query[List[String]]('reasons) :> Delete[User])

  val (get :|: wobPut :|: wbPut :|: wbPost :|: wobPost :|: delete :|: =:) = compile(api)

  implicit val cm = ClientManager(Http1Client[IO]().unsafeRunSync, "http://localhost", 9001)

  import TestServer.{decoder, encoder}

  "http4s client" >> {
    "without request body" >> {
      get("foo", 27).run[IO].unsafeRunSync() === User("foo", 27)
      wobPut().run[IO].unsafeRunSync() === User("joe", 27)
      wobPost().run[IO].unsafeRunSync() === User("joe", 27)
    }

    "with request body" >> {
      wbPut(User("joe", 27)).run[IO].unsafeRunSync()
      wbPost(User("joe", 27)).run[IO].unsafeRunSync() === User("joe", 27)
    }

    step {
      server.shutdown.unsafeRunSync()
    }
  }
}
