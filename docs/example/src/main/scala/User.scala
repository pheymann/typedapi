
import cats.effect.IO
import io.circe.syntax._
import io.circe.generic.JsonCodec
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.server.Server
import org.http4s.server.blaze._

final case class User(name: String, age: Int)

object User {

  implicit val enc = io.circe.generic.semiauto.deriveEncoder[User]
  implicit val dec = io.circe.generic.semiauto.deriveDecoder[User]

  implicit val decoder = jsonOf[IO, User]
  implicit val encoder = jsonEncoderOf[IO, User]
}
