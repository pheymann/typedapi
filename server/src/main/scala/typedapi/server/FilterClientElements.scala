package typedapi.server

import typedapi.shared.{ClientHeaderElement, ClientHeaderParam}
import shapeless._

sealed trait FilterClientElements[H <: HList] {

  type Out <: HList
}

sealed trait FilterClientElementsLowPrio {

  implicit val filterClientResult = new FilterClientElements[HNil] {
    type Out = HNil
  }

  implicit def filterClientKeep[El, T <: HList](implicit next: FilterClientElements[T]) = new FilterClientElements[El :: T] {
    type Out = El :: next.Out
  }
}

object FilterClientElements extends FilterClientElementsLowPrio {

  type Aux[H <: HList, Out0 <: HList] = FilterClientElements[H] { type Out = Out0 }

  implicit def filterClientEl[K, V, T <: HList](implicit next: FilterClientElements[T]) = new FilterClientElements[ClientHeaderElement[K, V] :: T] {
    type Out = next.Out
  }

  implicit def filterClientParam[K, V, T <: HList](implicit next: FilterClientElements[T]) = new FilterClientElements[ClientHeaderParam[K, V] :: T] {
    type Out = next.Out
  }
}

sealed trait FilterClientElementsList[H <: HList] {

  type Out <: HList
}

object FilterClientElementsList {

  type Aux[H <: HList, Out0 <: HList] = FilterClientElementsList[H] { type Out = Out0 }

  implicit val filterClientListResult = new FilterClientElementsList[HNil] {
    type Out = HNil
  }

  implicit def filterClientListStep[Api <: HList, T <: HList](implicit filtered: FilterClientElements[Api], next: FilterClientElementsList[T]) = 
    new FilterClientElementsList[Api :: T] {
      type Out = filtered.Out :: next.Out
    }
}
