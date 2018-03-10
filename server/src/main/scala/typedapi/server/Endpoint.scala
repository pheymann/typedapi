package typedapi.server

import shapeless.HList

final case class Endpoint[El <: HList, In <: HList, Out, Fun](fun: EndpointFunction.Aux[In, Out, Fun], f: Fun) {

  def apply(in: In): Out = fun(in, f)
}

final class EndpointDefinition[El <: HList, In <: HList, Out, Fun](fun: EndpointFunction.Aux[In, Out, Fun]) {

  def to(f: Fun): Endpoint[El, In, Out, Fun] = Endpoint(fun, f)
}

final class EndpointCompositionDefintion[C <: EndpointComposition] {

  def to(compostion: C): C = compostion
}
