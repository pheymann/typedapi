package http.support.tests.server

import http.support.tests.{User, UserCoding}
import typedapi.dsl._
import org.http4s.{Headers, Header, Request, Uri}
import org.http4s.dsl.io._
import org.http4s.client.blaze._
import org.http4s.client.dsl.io._
import cats.Applicative
import cats.effect.IO
import org.specs2.mutable.Specification

import scala.language.higherKinds

abstract class ServerSupportSpec[F[_]: Applicative] extends Specification {

  sequential

  val client = Http1Client[IO]().unsafeRunSync

  def tests(port: Int) = {
    import UserCoding._

    "paths and segments" >> {
        client.expect[User](s"http://localhost:$port/path").unsafeRunSync() === User("joe", 27)
        client.expect[User](s"http://localhost:$port/segment/jim").unsafeRunSync() === User("jim", 27)
      }
  
      "queries" >> {
        client.expect[User](s"http://localhost:$port/query?age=42").unsafeRunSync() === User("joe", 42)
      }
 
      "headers" >> {
        client.expect[User](Request[IO](method = GET, uri = Uri.fromString(s"http://localhost:$port/header").right.get, headers = Headers(Header("age", "42")))).unsafeRunSync() === User("joe", 42)
        client.expect[User](Request[IO](method = GET, uri =Uri.fromString(s"http://localhost:$port/header/raw").right.get, headers = Headers(Header("age", "42"), Header("name", "jim")))).unsafeRunSync() === User("jim", 42)
      }

      "methods" >> {
        client.expect[User](s"http://localhost:$port/").unsafeRunSync() === User("joe", 27)
        client.expect[User](PUT(Uri.fromString(s"http://localhost:$port/").right.get)).unsafeRunSync() === User("joe", 27)
        client.expect[User](PUT(Uri.fromString(s"http://localhost:$port/body").right.get, User("joe", 27))).unsafeRunSync() === User("joe", 27)
        client.expect[User](POST(Uri.fromString(s"http://localhost:$port/").right.get)).unsafeRunSync() === User("joe", 27)
        client.expect[User](POST(Uri.fromString(s"http://localhost:$port/body").right.get, User("joe", 27))).unsafeRunSync() === User("joe", 27)
        client.expect[User](DELETE(Uri.fromString(s"http://localhost:$port/?reasons=because").right.get)).unsafeRunSync() === User("joe", 27)
      }
  }

  val Api =
    (:= :> "path" :> Get[User]) :|:
    (:= :> "segment" :> Segment[String]('name) :> Get[User]) :|:
    (:= :> "query" :> Query[Int]('age) :> Get[User]) :|:
    (:= :> "header" :> typedapi.Header[Int]('age) :> Get[User]) :|:
    (:= :> "header" :> "raw" :> typedapi.Header[Int]('age) :> RawHeaders :> Get[User]) :|:
    (:= :> Get[User]) :|:
    (:= :> Put[User]) :|:
    (:= :> "body" :> ReqBody[User] :> Put[User]) :|:
    (:= :> Post[User]) :|:
    (:= :> "body" :> ReqBody[User] :> Post[User]) :|:
    (:= :> Query[List[String]]('reasons) :> Delete[User])

  val path: () => F[User] = () => Applicative[F].pure(User("joe", 27))
  val segment: String => F[User] = name => Applicative[F].pure(User(name, 27))
  val query: Int => F[User] = age => Applicative[F].pure(User("joe", age))
  val header: Int => F[User] = age => Applicative[F].pure(User("joe", age))
  val raw: (Int, Map[String, String]) => F[User] = (age, nameM) => Applicative[F].pure(User(nameM("name"), age))
  val get: () => F[User] = () => Applicative[F].pure(User("joe", 27))
  val put: () => F[User] = () => Applicative[F].pure(User("joe", 27))
  val putB: User => F[User] = user => Applicative[F].pure(user)
  val post: () => F[User] = () => Applicative[F].pure(User("joe", 27))
  val postB: User => F[User] = user => Applicative[F].pure(user)
  val delete: List[String] => F[User] = reasons => {
    println(reasons)
    Applicative[F].pure(User("joe", 27))
  }
}
