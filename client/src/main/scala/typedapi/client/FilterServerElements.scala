package typedapi.client

import typedapi.shared._
import shapeless._

sealed trait FilterServerElementsLowPrio extends TplPoly2 {

  implicit def filterKeepElement[H, In <: HList] = at[H, In, H :: In]
}

object FilterServerElements extends FilterServerElementsLowPrio {

  implicit def filterServerHeaderMatch[K, V, In <: HList] = at[ServerHeaderMatchParam[K, V], In, In]
  implicit def filterServerHeaderSend[K, V, In <: HList]  = at[ServerHeaderSendElement[K, V], In, In]
}
