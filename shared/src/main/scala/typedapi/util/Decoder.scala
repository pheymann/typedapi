package typedapi.util

import scala.language.higherKinds

trait Decoder[F[_], A] extends (String => F[Either[Exception, A]])

object Decoder {

  def apply[F[_], A](decoder: String => F[Either[Exception, A]]): Decoder[F, A] = new Decoder[F, A] {
    def apply(raw: String): F[Either[Exception, A]] = decoder(raw)
  }
}
