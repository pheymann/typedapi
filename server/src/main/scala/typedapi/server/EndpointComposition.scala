package typedapi.server

import shapeless._

sealed trait EndpointComposition

final case class :|:[Fun, T <: EndpointComposition](endpoint: Fun, tail: T) extends EndpointComposition {

  def :|:[FunH](funH: FunH): FunH :|: Fun :|: T = typedapi.server.:|:(funH, this)
}

case object =: extends EndpointComposition {

  def :|:[Fun](endpoint: Fun) = typedapi.server.:|:(endpoint, this)
}

sealed trait HListToComposition[H <: HList] {

  type Out <: EndpointComposition
}

trait HListToCompositionLowPrio {

  implicit val hnilComposition = new HListToComposition[HNil] {
    type Out = =:.type
  }

  implicit def consComposition[H <: HList, El <: HList, In <: HList, CIn <: HList, Out, Fun, T <: HList](implicit next: HListToComposition[T], fun: EndpointFunction.Aux[In, CIn, Out, Fun]) = new HListToComposition[(El, In, Out) :: T] {

    type Out = :|:[Fun, next.Out]
  }
}
