
import typedapi.client._
import typedapi.client.http4s._

object Client {

  val (fetch :|: create :|: =:) = compile(FromDsl.MyApi)

  def main(args: Array[String]): Unit = {
    import User._
    import cats.effect.IO
    import org.http4s.client.blaze.Http1Client

    implicit val cm = ClientManager(Http1Client[IO]().unsafeRunSync, "http://localhost", 9000)

    (for {
      u0 <- create(User("joe", 27)).run[IO]
      u1 <- fetch("joe").run[IO]
    } yield {
      println(u0)
      println(u1)
      ()
    }).unsafeRunSync()
  }
}
