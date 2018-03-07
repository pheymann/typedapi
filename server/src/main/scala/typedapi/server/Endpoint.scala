package typedapi.server

import shapeless.HList

final case class Endpoint[El <: HList, In <: HList, Fun](f: Fun)

final class EndpointDefinition[El <: HList, In <: HList, Out, Fun](fun: EndpointFunction.Aux[In, Out, Fun]) {

  def :=(f: Fun): Endpoint[El, In, Fun] = to(f)
  def to(f: Fun): Endpoint[El, In, Fun] = Endpoint[El, In, Fun](f)
}

final class EndpointCompositionDefintion[C <: EndpointComposition] {

  def to(compostion: C): C = compostion
}
