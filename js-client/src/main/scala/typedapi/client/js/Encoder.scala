package typedapi.client.js

import scala.language.higherKinds

trait Encoder[F[_], A] extends (A => F[String])

object Encoder {

  def apply[F[_], A](encoder: A => F[String]): Encoder[F, A] = new Encoder[F, A] {
    def apply(a: A): F[String] = encoder(a)
  }
}
