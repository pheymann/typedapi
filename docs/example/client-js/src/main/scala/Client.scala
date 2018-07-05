
import typedapi.client._
import typedapi.client.js._
import org.scalajs.dom.ext.Ajax
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Client {

  type Id[A] = A

  final case class DecodeException(msg: String) extends Exception

  implicit val decoder = typedapi.client.js.Decoder[Future, User](json => decode[User](json).fold(
    error => Future.success(Left(DecodeException(error.toString()))), 
    Right(_)
  ))
  implicit val encoder = typedapi.client.js.Encoder[Future, User](user => Future.successful(user.asJson.noSpaces))

  val (fetch, create) = deriveAll(FromDsl.MyApi)

  def main(args: Array[String]): Unit = {
    val cm = ClientManager(Ajax, "http://localhost", 9000)

    (for {
      u0 <- create(User("joe", 27)).run[Future](cm)
      u1 <- fetch("joe").run[Future](cm)
    } yield (u0, u1)).foreach { case (u0, u1) =>
      println(u0)
      println(u1)
    }
  }
}
