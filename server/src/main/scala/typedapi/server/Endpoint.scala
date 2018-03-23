package typedapi.server

import shapeless._

import scala.language.higherKinds

/** Container storing the extractor and function of an endpoint. */
abstract class Endpoint[El <: HList, In <: HList, ROut, CIn <: HList, F[_], Out](val extractor: RouteExtractor.Aux[El, In, HNil, ROut]) {

  def apply(in: CIn): F[Out]
}

/** Request representation which every server implementation has to provide. */
final case class EndpointRequest(method: String, 
                                 uri: List[String],
                                 queries: Map[String, List[String]],
                                 headers: Map[String, String])

final class EndpointDefinition[El <: HList, In <: HList, ROut, CIn <: HList, Fun[_[_]], Out](extractor: RouteExtractor.Aux[El, In, HNil, ROut], 
                                                                                             funApply: FunctionApply.Aux[In, CIn, Fun, Out]) {

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
  def to[F[_]](f: Fun[F]): Endpoint[El, In, ROut, CIn, F, Out] = 
    new Endpoint[El, In, ROut, CIn, F, Out](extractor) {
      def apply(in: CIn): F[Out] = funApply(in, f)
    }
}
