
import typedapi.server._
import typedapi.server.http4s._
import cats.effect.IO
import org.http4s._
import org.http4s.circe._

object Server {

  implicit val decoder = jsonOf[IO, User]
  implicit val encoder = jsonEncoderOf[IO, User]

  val get: () => IO[User] = () => IO.pure(User("Joe", 42))
  val put: () => IO[User] = get
  val post: () => IO[User] = get
  val delete: () => IO[User] = get

  val path: () => IO[User] = get

  val putBody: User => IO[User] = user => IO.pure(user)
  val segment: String => IO[User] = name => IO.pure(User(name, 42))
  val search: String => IO[User] = segment

  val header: String => IO[User] = consumer => IO.pure(User("found: " + consumer, 42))
  val fixed: () => IO[User] = get
  val client: () => IO[User] = get

  val endpoints = deriveAll[IO](FromDefinition.MyApi).from(get, put, post, delete, path, putBody, segment, search, header, fixed, client)

  def main(args: Array[String]): Unit = {
    import org.http4s.server.blaze.BlazeBuilder

    val sm = ServerManager(BlazeBuilder[IO], "localhost", 9000)

    mount(sm, endpoints).unsafeRunSync()

    scala.io.StdIn.readLine("Press 'Enter' to stop ...")
  }
}
