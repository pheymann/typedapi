package typedapi.server

import typedapi.shared._
import shapeless._

sealed trait FilterClientElementsLowPrio extends TplPoly2 {

  implicit def filterKeepElement[H, In <: HList] = at[H, In, H :: In]
}

object FilterClientElements extends FilterClientElementsLowPrio {

  implicit def filterClientHeaderEl[K, V, In <: HList] = at[ClientHeaderElement[K, V], In, In]
  implicit def filterClientHeaderParam[K, V, In <: HList] = at[ClientHeaderParam[K, V], In, In]
  implicit def filterClientHeaderCollParam[V, In <: HList] = at[ClientHeaderCollParam[V], In, In]
}
