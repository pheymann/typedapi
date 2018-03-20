package typedapi.server.http4s

import org.http4s.server.Server
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.dsl.io._
import org.http4s.circe._
import io.circe.generic.JsonCodec
import scala.io.StdIn
import typedapi.server._
import cats.effect.IO

@JsonCodec final case class Foo(name: String, age: Int)

object TestServer {

  val Api =
    (:= :> "find" :> Segment[String]('name) :> Query[Int]('age) :> Get[Foo]) :|:
    (:= :> "create" :> ReqBody[Foo] :> Put[Foo])

  def find(name: String, age: Int): IO[Foo] = IO.pure(Foo(name, age))
  def create(foo: Foo): IO[Foo] = IO.pure(foo)

  implicit val decoder = jsonOf[IO, Foo]
  implicit val encoder = jsonEncoderOf[IO, Foo]

  val endpoints = link(Api).to[IO](find _ :|: create _ :|: =:)

  def apply(sm: ServerManager[BlazeBuilder[IO]]): IO[Server[IO]] = mount(sm, endpoints)

  def main(args: Array[String]): Unit = {
    val sm = ServerManager(BlazeBuilder[IO], "localhost", 9000)

    val server = TestServer(sm).unsafeRunSync()

    StdIn.readLine()
    server.shutdown.unsafeRunSync()
  }
}
