package typedapi.server

import typedapi.shared._
import shapeless._

import scala.annotation.implicitNotFound

@implicitNotFound("""Whoops, you should not be here. This seems to be a bug.

elements: ${El}""")
sealed trait ServerHeaderExtractor[El <: HList] {

  def apply(agg: Map[String, String]): Map[String, String]
}

sealed trait ServerHeaderExtractorLowPrio {

  implicit val serverHeaderReturnCase = new ServerHeaderExtractor[HNil] {
    def apply(agg: Map[String, String]): Map[String, String] = agg
  }

  implicit def serverHeaderIgnoreCase[H, T <: HList](implicit next: ServerHeaderExtractor[T]) = new ServerHeaderExtractor[H :: T] {
    def apply(agg: Map[String, String]): Map[String, String] = next(agg)
  }
}

object ServerHeaderExtractor extends ServerHeaderExtractorLowPrio {

  implicit def serverHeaderExtractCase[K, V, T <: HList]
      (implicit kWit: Witness.Aux[K], kShow: WitnessToString[K], vWit: Witness.Aux[V], vShow: WitnessToString[V], next: ServerHeaderExtractor[T]) = 
    new ServerHeaderExtractor[ServerHeaderSend[K, V] :: T] {
      def apply(agg: Map[String, String]): Map[String, String] = {
        val key   = kShow.show(kWit.value)
        val value = vShow.show(vWit.value)

        next(agg + (key -> value))
      }
    }
}
