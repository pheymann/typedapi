package http.support.tests.server

import typedapi.server.{Result, successWith, errorWith, Ok, BadRequest, InternalServerError}
import http.support.tests.{User, UserCoding}
import org.http4s._
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
      client.expect[User](Request[IO](
        method = GET,
        uri = Uri.fromString(s"http://localhost:$port/header").right.get,
        headers = Headers(Header("age", "42"))
      )).unsafeRunSync() === User("joe", 42)
      client.expect[User](Request[IO](
        method = GET,
        uri = Uri.fromString(s"http://localhost:$port/header/fixed").right.get,
        headers = Headers(Header("Hello", "*"))
      )).unsafeRunSync() === User("joe", 27)
      client.expect[User](Request[IO](
        method = GET,
        uri = Uri.fromString(s"http://localhost:$port/header/client").right.get
      )).unsafeRunSync() === User("joe", 27)
      client.expect[User](Request[IO](
        method = GET,
        uri = Uri.fromString(s"http://localhost:$port/header/input/client").right.get
      )).unsafeRunSync() === User("joe", 27)
      client.fetch[Option[Header]](
        Request[IO](
          method = GET,
          uri = Uri.fromString(s"http://localhost:$port/header/server/send").right.get
        )
      )(
        resp => IO {
          resp.headers.toList.find(_.name.toString == "Hello")
        }
      ).unsafeRunSync() === Some(Header("Hello", "*"))
      client.expect[User](Request[IO](
        method = GET,
        uri = Uri.fromString(s"http://localhost:$port/header/server/match").right.get,
        headers = Headers(Header("test", "foo"), Header("testy", "bar"), Header("meh", "NONO"))
      )).unsafeRunSync() === User("test -> foo,testy -> bar", 27)
      client.fetch[Option[Header]](
        Request[IO](
          method = OPTIONS,
          uri = Uri.fromString(s"http://localhost:$port/header/fixed").right.get,
          headers = Headers(Header("Hello", "*"))
        )
      )(
        resp => IO {
          resp.headers.toList.find(_.name.toString == "Access-Control-Allow-Methods")
        }
      ).unsafeRunSync() === Some(Header("Access-Control-Allow-Methods", "GET"))
    }

    "methods" >> {
      client.expect[User](s"http://localhost:$port/").unsafeRunSync() === User("joe", 27)
      client.expect[User](PUT(Uri.fromString(s"http://localhost:$port/").right.get)).unsafeRunSync() === User("joe", 27)
      client.expect[User](PUT(Uri.fromString(s"http://localhost:$port/body").right.get, User("joe", 27))).unsafeRunSync() === User("joe", 27)
      client.expect[User](POST(Uri.fromString(s"http://localhost:$port/").right.get)).unsafeRunSync() === User("joe", 27)
      client.expect[User](POST(Uri.fromString(s"http://localhost:$port/body").right.get, User("joe", 27))).unsafeRunSync() === User("joe", 27)
      client.expect[User](DELETE(Uri.fromString(s"http://localhost:$port/?reasons=because").right.get)).unsafeRunSync() === User("joe", 27)
    }

    "status codes" >> {
      client.fetch[Int](GET(Uri.fromString(s"http://localhost:$port/status/200").right.get))(resp => IO.pure(resp.status.code)).unsafeRunSync === 200
      client.fetch[Int](GET(Uri.fromString(s"http://localhost:$port/status/400").right.get))(resp => IO.pure(resp.status.code)).unsafeRunSync === 400
      client.fetch[Int](GET(Uri.fromString(s"http://localhost:$port/status/500").right.get))(resp => IO.pure(resp.status.code)).unsafeRunSync === 500
    }
  }

  val path: () => F[Result[User]] = () => Applicative[F].pure(successWith(Ok)(User("joe", 27)))
  val segment: String => F[Result[User]] = name => Applicative[F].pure(successWith(Ok)(User(name, 27)))
  val query: Int => F[Result[User]] = age => Applicative[F].pure(successWith(Ok)(User("joe", age)))
  val header: Int => F[Result[User]] = age => Applicative[F].pure(successWith(Ok)(User("joe", age)))
  val fixed: () => F[Result[User]] = () => Applicative[F].pure(successWith(Ok)(User("joe", 27)))
  val matching: Map[String, String] => F[Result[User]] = matches => Applicative[F].pure(successWith(Ok)(User(matches.mkString(","), 27)))
  val get: () => F[Result[User]] = () => Applicative[F].pure(successWith(Ok)(User("joe", 27)))
  val put: () => F[Result[User]] = () => Applicative[F].pure(successWith(Ok)(User("joe", 27)))
  val putB: User => F[Result[User]] = user => Applicative[F].pure(successWith(Ok)(user))
  val post: () => F[Result[User]] = () => Applicative[F].pure(successWith(Ok)(User("joe", 27)))
  val postB: User => F[Result[User]] = user => Applicative[F].pure(successWith(Ok)(user))
  val delete: List[String] => F[Result[User]] = reasons => {
    println(reasons)
    Applicative[F].pure(successWith(Ok)(User("joe", 27)))
  }
  val code200: () => F[Result[String]] = () => Applicative[F].pure(successWith(Ok)(""))
  val code400: () => F[Result[String]] = () => Applicative[F].pure(errorWith(BadRequest, "meh"))
  val code500: () => F[Result[String]] = () => Applicative[F].pure(errorWith(InternalServerError, "boom"))
}
