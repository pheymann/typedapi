package typedapi.server

import shapeless._

final case class Endpoint[El <: HList, In <: HList, ROut, CIn <: HList, Fun, Out](extractor: RouteExtractor.Aux[El, In, HNil, ROut],
                                                                                  fun: EndpointFunction.Aux[In, CIn, Fun, Out], 
                                                                                  f: Fun) {

  def apply(in: CIn): Out = fun(in, f)
}

final class EndpointDefinition[El <: HList, In <: HList, ROut, CIn <: HList, Fun, Out](extractor: RouteExtractor.Aux[El, In, HNil, ROut],
                                                                                       fun: EndpointFunction.Aux[In, CIn, Fun, Out]) {

  def to(f: Fun): Endpoint[El, In, ROut, CIn, Fun, Out] = Endpoint(extractor, fun, f)
}
