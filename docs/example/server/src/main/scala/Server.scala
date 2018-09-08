
import typedapi.server._
import typedapi.server.http4s._
import cats.effect.IO
import org.http4s._
import org.http4s.circe._

object Server {

  implicit val decoder = jsonOf[IO, User]
  implicit val encoder = jsonEncoderOf[IO, User]

  val get: () => IO[Result[User]] = () => IO.pure(success(User("Joe", 42)))
  val put: () => IO[Result[User]] = get
  val post: () => IO[Result[User]] = get
  val delete: () => IO[Result[User]] = get

  val path: () => IO[Result[User]] = get

  val putBody: User => IO[Result[User]] = user => IO.pure(success(user))
  val segment: String => IO[Result[User]] = name => IO.pure(success(User(name, 42)))
  val search: String => IO[Result[User]] = segment

  val header: String => IO[Result[User]] = consumer => IO.pure(success(User("found: " + consumer, 42)))
  val fixed: () => IO[Result[User]] = get
  val client: () => IO[Result[User]] = get
  val coll: () => IO[Result[User]] = get
  val matching: Map[String, String] => IO[Result[User]] = matches => IO.pure(success(User(matches.mkString(","), 42)))

  val endpoints = deriveAll[IO](FromDefinition.MyApi).from(
    get, 
    put, 
    post, 
    delete, 
    path, 
    putBody, 
    segment, 
    search, 
    header, 
    fixed, 
    client, 
    coll, 
    matching
  )

  def main(args: Array[String]): Unit = {
    import org.http4s.server.blaze.BlazeBuilder

    val sm = ServerManager(BlazeBuilder[IO], "localhost", 9000)

    mount(sm, endpoints).unsafeRunSync()

    scala.io.StdIn.readLine("Press 'Enter' to stop ...")
  }
}
