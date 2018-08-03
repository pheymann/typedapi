package typedapi.client

import typedapi.shared.ServerHeaderElement
import shapeless._

//TODO replace with Typelevelfoldleft
sealed trait FilterServerElements[H <: HList] {

  type Out <: HList
}

sealed trait FilterServerElementsLowPrio {

  implicit val filterServerResult = new FilterServerElements[HNil] {
    type Out = HNil
  }

  implicit def filterServerKeep[El, T <: HList](implicit next: FilterServerElements[T]) = new FilterServerElements[El :: T] {
    type Out = El :: next.Out
  }
}

object FilterServerElements extends FilterServerElementsLowPrio {

  type Aux[H <: HList, Out0 <: HList] = FilterServerElements[H] { type Out = Out0 }

  implicit def filterServerEl[K, V, T <: HList](implicit next: FilterServerElements[T]) = new FilterServerElements[ServerHeaderElement[K, V] :: T] {
    type Out = next.Out
  }
}

sealed trait FilterServerElementsList[H <: HList] {

  type Out <: HList
}

object FilterServerElementsList {

  type Aux[H <: HList, Out0 <: HList] = FilterServerElementsList[H] { type Out = Out0 }

  implicit val filterServerListResult = new FilterServerElementsList[HNil] {
    type Out = HNil
  }

  implicit def filterServerListStep[Api <: HList, T <: HList](implicit filtered: FilterServerElements[Api], next: FilterServerElementsList[T]) = 
    new FilterServerElementsList[Api :: T] {
      type Out = filtered.Out :: next.Out
    }
}
