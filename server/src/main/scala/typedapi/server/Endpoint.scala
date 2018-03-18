package typedapi.server

import shapeless._

import scala.language.higherKinds

abstract class Endpoint[El <: HList, In <: HList, ROut, CIn <: HList, F[_], Out](val extractor: RouteExtractor.Aux[El, In, HNil, ROut]) {

  def apply(in: CIn): F[Out]
}

final class EndpointDefinition[El <: HList, In <: HList, ROut, CIn <: HList, Fun[_[_]], Out](extractor: RouteExtractor.Aux[El, In, HNil, ROut], 
  funDef: FunctionDef.Aux[In, CIn, Fun, Out]) {

  def to[F[_]](f: Fun[F]): Endpoint[El, In, ROut, CIn, F, Out] = 
    new Endpoint[El, In, ROut, CIn, F, Out](extractor) {
      def apply(in: CIn): F[Out] = funDef(in, f)
    }
}
