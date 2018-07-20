package http.support.tests.server

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
        uri = Uri.fromString(s"http://localhost:$port/header/client").right.get,
        headers = Headers(Header("Hello", "*"))
      )).unsafeRunSync() === User("joe", 27)
      client.fetch[Option[Header]](
        Request[IO](
          method = GET,
          uri = Uri.fromString(s"http://localhost:$port/header/server").right.get
        )
      )(
        resp => IO {
          resp.headers.toList.find(_.name.toString == "Hello")
        }
      ).unsafeRunSync() === Some(Header("Hello", "*"))
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
  }

  val path: () => F[User] = () => Applicative[F].pure(User("joe", 27))
  val segment: String => F[User] = name => Applicative[F].pure(User(name, 27))
  val query: Int => F[User] = age => Applicative[F].pure(User("joe", age))
  val header: Int => F[User] = age => Applicative[F].pure(User("joe", age))
  val fixed: () => F[User] = () => Applicative[F].pure(User("joe", 27))
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
