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

  val ApiDsl = {
    import typedapi.dsl._

    (:= :> "path" :> Get[User]) :|:
    (:= :> "segment" :> Segment[String]('name) :> Get[User]) :|:
    (:= :> "query" :> Query[Int]('age) :> Get[User]) :|:
    (:= :> "header" :> Header[Int]('age) :> Get[User]) :|:
    (:= :> "header" :> "raw" :> Header[Int]('age) :> RawHeaders :> Get[User]) :|:
    (:= :> Get[User]) :|:
    (:= :> Put[User]) :|:
    (:= :> "body" :> ReqBody[User] :> Put[User]) :|:
    (:= :> Post[User]) :|:
    (:= :> "body" :> ReqBody[User] :> Post[User]) :|:
    (:= :> Query[List[String]]('reasons) :> Delete[User])
  }

  val ApiDef = {
    import typedapi._

    api(Get[User], Root / "path") :|:
    api(Get[User], Root / "segment" / Segment[String]('name)) :|:
    api(Get[User], Root / "query", Queries add Query[Int]('age)) :|:
    api(Get[User], Root / "header", headers = Headers add Header[Int]('age)) :|:
    api(Get[User], Root / "header" / "raw", headers = Headers add Header[Int]('age) add RawHeaders) :|:
    api(Get[User]) :|:
    api(Put[User]) :|:
    apiWithBody(Put[User], ReqBody[User], Root / "body") :|:
    api(Post[User]) :|:
    apiWithBody(Post[User], ReqBody[User], Root / "body") :|:
    api(Delete[User], Root, Queries add Query[List[String]]('reasons))
  }

  val path: () => IO[User] = () => IO.pure(User("joe", 27))
  val segment: String => IO[User] = name => IO.pure(User(name, 27))
  val query: Int => IO[User] = age => IO.pure(User("joe", age))
  val header: Int => IO[User] = age => IO.pure(User("joe", age))
  val raw: (Int, Map[String, String]) => IO[User] = (age, nameM) => IO.pure(User(nameM("name"), age))
  val get: () => IO[User] = () => IO.pure(User("joe", 27))
  val put: () => IO[User] = () => IO.pure(User("joe", 27))
  val putB: User => IO[User] = user => IO.pure(user)
  val post: () => IO[User] = () => IO.pure(User("joe", 27))
  val postB: User => IO[User] = user => IO.pure(user)
  val delete: List[String] => IO[User] = reasons => {
    println(reasons)
    IO.pure(User("joe", 27))
  }

  implicit val decoder = jsonOf[IO, User]
  implicit val encoder = jsonEncoderOf[IO, User]

  val endpointsDsl = deriveAll[IO](ApiDsl).from(path :|: segment :|: query :|: header :|: raw :|: get :|: put :|: putB :|: post :|: postB :|: delete :|: =:)
  val endpointsDef = deriveAll[IO](ApiDef).from(path :|: segment :|: query :|: header :|: raw :|: get :|: put :|: putB :|: post :|: postB :|: delete :|: =:)

  def startDsl(blaze: BlazeBuilder[IO]): IO[Server[IO]] = {
    val sm = ServerManager(blaze, "localhost", 9000)

    mount(sm, endpointsDsl)
  }

  def startDef(blaze: BlazeBuilder[IO]): IO[Server[IO]] = {
    val sm = ServerManager(blaze, "localhost", 10000)

    mount(sm, endpointsDef)
  }
}
