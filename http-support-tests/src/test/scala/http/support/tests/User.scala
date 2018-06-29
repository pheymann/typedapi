package http.support.tests

import cats.effect.IO
import io.circe.generic.JsonCodec
import org.http4s.circe._
import org.http4s.dsl.io._

@JsonCodec case class User(name: String, age: Int)

object UserCoding {

  implicit val decoderIO = jsonOf[IO, User]
  implicit val encoderIO = jsonEncoderOf[IO, User]
}
