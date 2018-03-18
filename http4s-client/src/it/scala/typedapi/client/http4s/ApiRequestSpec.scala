package typedapi.client.http4s

import typedapi.client._

import cats.effect.IO
import org.http4s.client.blaze.Http1Client
import org.specs2.mutable.Specification

final class ApiRequestSpec extends Specification {

  val TestGet = := :> "get" :> Segment[String]('name) :> Query[Int]('age) :> Get[User]
  val testGet = compile(TestGet)

  val api = 
    (:= :> "put" :> ReqBody[User] :> Put[User]) :|:
    (:= :> "put" :> ReqBody[User] :> Put[Unit]) :|:
    (:= :> "post" :> ReqBody[User] :> Post[User]) :|:
    (:= :> "delete" :> Segment[String]('name) :> Query[List[String]]('reasons) :> Delete[User])

  val (testPut0 :|: testPut1 :|: testPost :|: testDelete :|: =:) = compile(api)

  implicit val cm = ClientManager(Http1Client[IO]().unsafeRunSync, "http://localhost", 9090)

  import TestServer.{decoder, encoder}

  "http4s client" >> {
    testGet("foo", 27).run[IO].unsafeRunSync() === User("foo", 27)
    testPut0(User("foo", 27)).run[IO].unsafeRunSync() === User("foo", 27)
    testPut1(User("foo", 27)).run[IO].unsafeRunSync()
    testPost(User("foo", 27)).run[IO].unsafeRunSync() === User("foo", 27)
    testDelete("bar", List("foo", "bar")).run[IO].unsafeRunSync() === User("bar", 30)
  }
}
