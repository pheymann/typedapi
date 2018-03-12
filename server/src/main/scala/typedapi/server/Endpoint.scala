package typedapi.server

import shapeless._

final case class Endpoint[El <: HList, In <: HList, CIn <: HList, RCIn <: HList, Out, Fun](fun: EndpointFunction.Aux[In, CIn, Out, Fun], 
                                                                                           f: Fun,
                                                                                           extractor: RouteExtractor.Aux[El, In, HNil, RCIn]) {

  def apply(in: CIn): Out = fun(in, f)
}

final class EndpointDefinition[El <: HList, In <: HList, CIn <: HList, RCIn <: HList, Out, Fun](fun: EndpointFunction.Aux[In, CIn, Out, Fun],
                                                                                                extractor: RouteExtractor.Aux[El, In, HNil, RCIn]) {

  def to(f: Fun): Endpoint[El, In, CIn, RCIn, Out, Fun] = Endpoint(fun, f, extractor)
}

final class EndpointCompositionDefintion[C <: EndpointComposition] {

  def to(compostion: C): C = compostion
}
