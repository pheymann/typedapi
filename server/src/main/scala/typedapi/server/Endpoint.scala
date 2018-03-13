package typedapi.server

import shapeless._

final case class Endpoint[El <: HList, In <: HList, ROut, CIn <: HList, Out, Fun](extractor: RouteExtractor.Aux[El, In, HNil, ROut],
                                                                                  fun: EndpointFunction.Aux[In, CIn, Out, Fun], 
                                                                                  f: Fun) {

  def apply(in: CIn): Out = fun(in, f)
}

final class EndpointDefinition[El <: HList, In <: HList, ROut, CIn <: HList, Out, Fun](extractor: RouteExtractor.Aux[El, In, HNil, ROut],
                                                                                       fun: EndpointFunction.Aux[In, CIn, Out, Fun]) {

  def to(f: Fun): Endpoint[El, In, ROut, CIn, Out, Fun] = Endpoint(extractor, fun, f)
}

final class EndpointCompositionDefintion[C <: EndpointComposition] {

  def to(compostion: C): C = compostion
}
