package typedapi.server.http4s

import typedapi.server._
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.dsl.io._
import org.http4s.circe._
import io.circe.generic.JsonCodec
import cats.effect.IO

@JsonCodec final case class User(name: String, age: Int)

object TestServer {

  val Api =
    (:= :> "without" :> "reqbody" :> Get[User]) :|:
    (:= :> "with" :> "reqbody" :> ReqBody[User] :> Put[User]) :|:
    (:= :> "raw" :> Get[String]) :|:
    (:= :> "query" :> Query[Int]('query) :> Get[String])

  val withoutReqBody: () => IO[User] = () => IO.pure(User("joe", 27))
  val withReqBody: User => IO[User] = user => IO.pure(user)
  val raw: () => IO[String] = () =>  IO.pure("hello")
  val query: Int => IO[String] = query => IO.pure(query.toString())

  implicit val decoder = jsonOf[IO, User]
  implicit val encoder = jsonEncoderOf[IO, User]

  val endpoints = link(Api).to[IO](withoutReqBody :|: withReqBody :|: raw :|: query :|: =:)

  def start(blaze: BlazeBuilder[IO]): IO[Server[IO]] = {
    val sm = ServerManager(blaze, "localhost", 9000)

    mount(sm, endpoints)
  }
}
