package typedapi.server

import shapeless.HList

final case class Endpoint[El <: HList, In <: HList, CIn <: HList, Out, Fun](fun: EndpointFunction.Aux[In, CIn, Out, Fun], f: Fun) {

  def apply(in: CIn): Out = fun(in, f)
}

final class EndpointDefinition[El <: HList, In <: HList, CIn <: HList, Out, Fun](fun: EndpointFunction.Aux[In, CIn, Out, Fun]) {

  def to(f: Fun): Endpoint[El, In, CIn, Out, Fun] = Endpoint(fun, f)
}

final class EndpointCompositionDefintion[C <: EndpointComposition] {

  def to(compostion: C): C = compostion
}
