
import io.circe.syntax._
import io.circe.generic.JsonCodec

final case class User(name: String, age: Int)

object User {

  implicit val enc = io.circe.generic.semiauto.deriveEncoder[User]
  implicit val dec = io.circe.generic.semiauto.deriveDecoder[User]
}
