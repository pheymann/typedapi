package typedapi.client.js

trait Encoder[A] extends (A => String)

object Encoder {

  def apply[A](encoder: A => String): Encoder[A] = new Encoder[A] {
    def apply(a: A): String = encoder(a)
  }
}
