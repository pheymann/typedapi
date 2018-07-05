package http.support.tests

import cats.effect.IO
import io.circe.generic.semiauto._
import org.http4s.circe._
import org.http4s.dsl.io._

final case class User(name: String, age: Int)

object UserCoding {

  implicit val enc = deriveEncoder[User]
  implicit val dec = deriveDecoder[User]

  implicit val decoderIO = jsonOf[IO, User]
  implicit val encoderIO = jsonEncoderOf[IO, User]
}
