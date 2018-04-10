
import typedapi.server._
import typedapi.server.http4s._
import cats.effect.IO

object Server {

  val fetch: String => IO[User] = name => IO.pure(User(name, 27))
  val create: User => IO[User] = user => IO.pure(user)

  val endpoints = link(FromDefinition.MyApi).to[IO](fetch :|: create :|: =:)

  def main(args: Array[String]): Unit = {
    import org.http4s.server.blaze.BlazeBuilder

    val sm = ServerManager(BlazeBuilder[IO], "localhost", 9000)

    mount(sm, endpoints).unsafeRunSync()

    scala.io.StdIn.readLine("Press 'Enter' to stop ...")
  }
}
