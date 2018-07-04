package typedapi.client.js

import scala.concurrent.Future

trait Decoder[A] extends (String => Future[A])

object Decoder {

  def apply[A](decoder: String => Future[A]): Decoder[A] = new Decoder[A] {
    def apply(raw: String): Future[A] = decoder(raw)
  }
}
