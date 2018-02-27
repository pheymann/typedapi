package typedapi.client.http4s

import cats.effect.IO
import io.circe.syntax._
import io.circe.generic.JsonCodec
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.server.blaze._

import scala.io.StdIn

@JsonCodec case class User(name: String, age: Int)

object Age extends QueryParamDecoderMatcher[Int]("age")
object Reasons {
  def unapplySeq(params: Map[String, Seq[String]]) = params.get("reasons")
  def unapply(params: Map[String, Seq[String]]) = unapplySeq(params)
}

object TestServer {

  implicit val decoder = jsonOf[IO, User]
  implicit val encoder = jsonEncoderOf[IO, User]
   
  val service = HttpService[IO] {
    case GET -> Root / "get" / name :? Age(age) => Ok(User(name, age))

    case req @ PUT -> Root / "put" =>
      for {
        user <- req.as[User]
        resp <- Ok(user.asJson)
      } yield resp

    case req @ POST -> Root / "post" =>
      for {
        user <- req.as[User]
        resp <- Ok(user.asJson)
      } yield resp

    case DELETE -> Root / "delete" / name :? Reasons(reasons) => 
      println(reasons)
      Ok(User(name, 30))
  }

  def main(args: Array[String]): Unit = {
    val port = args(0).toInt

    val builder = BlazeBuilder[IO]
      .bindHttp(port, "localhost")
      .mountService(service, "/")
      .start

    val server = builder.unsafeRunSync()

    StdIn.readLine()
    server.shutdown.unsafeRunSync()
  }
}
