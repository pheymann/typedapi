
import typedapi.client._
import typedapi.client.http4s._

object Client {

  val (fetch :|: create :|: =:) = deriveAll(FromDsl.MyApi)

  def main(args: Array[String]): Unit = {
    import User._
    import cats.effect.IO
    import org.http4s.client.blaze.Http1Client

    val cm = ClientManager(Http1Client[IO]().unsafeRunSync, "http://localhost", 9000)

    (for {
      u0 <- create(User("joe", 27)).run[IO](cm)
      u1 <- fetch("joe").run[IO](cm)
    } yield {
      println(u0)
      println(u1)
      ()
    }).unsafeRunSync()
  }
}
