
import typedapi.server._
import typedapi.server.http4s._
import cats.effect.IO
import org.http4s._
import org.http4s.circe._

object Server {

  implicit val decoder = jsonOf[IO, User]
  implicit val encoder = jsonEncoderOf[IO, User]

  val fetch: String => IO[User] = name => IO.pure(User(name, 27))
  val create: User => IO[User] = user => IO.pure(user)

  val endpoints = deriveAll[IO](FromDefinition.MyApi).from(fetch, create)

  def main(args: Array[String]): Unit = {
    import org.http4s.server.blaze.BlazeBuilder

    val sm = ServerManager(BlazeBuilder[IO], "localhost", 9000)

    mount(sm, endpoints).unsafeRunSync()

    scala.io.StdIn.readLine("Press 'Enter' to stop ...")
  }
}
