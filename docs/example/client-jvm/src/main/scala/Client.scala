
import typedapi.client._
import typedapi.client.http4s._
import cats.effect.IO
import org.http4s._
import org.http4s.circe._

object Client {

  implicit val decoder = jsonOf[IO, User]
  implicit val encoder = jsonEncoderOf[IO, User]

  val (get, put, post, delete, path, putBody, segment, search, header, fixed, client, coll, matches) = deriveAll(FromDsl.MyApi)

  def main(args: Array[String]): Unit = {
    import User._
    import cats.effect.IO
    import org.http4s.client.blaze.Http1Client

    val cm = ClientManager(Http1Client[IO]().unsafeRunSync, "http://localhost", 9000)

    (for {
      u0 <- putBody(User("joe", 27)).run[IO](cm)
      u1 <- search("joe").run[IO](cm)
    } yield {
      println(u0)
      println(u1)
      ()
    }).unsafeRunSync()
  }
}
