package typedapi.server

import shapeless._

import scala.language.higherKinds

/** Container storing the extractor and function of an endpoint. */
abstract class Endpoint[El <: HList, KIn <: HList, VIn <: HList, ROut, F[_], Out](val extractor: RouteExtractor.Aux[El, KIn, VIn, HNil, ROut]) {

  def apply(in: VIn): F[Out]
}

/** Request representation which every server implementation has to provide. */
final case class EndpointRequest(method: String, 
                                 uri: List[String],
                                 queries: Map[String, List[String]],
                                 headers: Map[String, String])

final class EndpointDefinition[El <: HList, KIn <: HList, VIn <: HList, ROut, Fun[_[_]], Out](extractor: RouteExtractor.Aux[El, KIn, VIn, HNil, ROut], 
                                                                                              funApply: FunctionApply.Aux[VIn, Fun, Out]) {

  /** Restricts type of parameter `f` to a function defined by the given API:
    * 
    * {{{
    * val Api = := :> Segment[String]('name) :> Get[User]
    * 
    * link(Api).to[IO](name: String => IO.pure(User(name)))
    * }}}
    * 
    * Generates an `Endpoint` from `f`, the extractor and `FunctionApply` instance.
    */
  def to[F[_]](f: Fun[F]): Endpoint[El, KIn, VIn, ROut, F, Out] = 
    new Endpoint[El, KIn, VIn, ROut, F, Out](extractor) {
      def apply(in: VIn): F[Out] = funApply(in, f)
    }
}
