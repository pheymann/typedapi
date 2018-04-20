package typedapi.server

import typedapi.shared._
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

final class ExecutableDerivation[F[_]] {

  final class Derivation[El <: HList, KIn <: HList, VIn <: HList, ROut, Fn, Out](extractor: RouteExtractor.Aux[El, KIn, VIn, HNil, ROut], 
                                                                                 fnApply: FunctionApply.Aux[VIn, Fn, F, Out]) {

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
    def from(f: Fn): Endpoint[El, KIn, VIn, ROut, F, Out] =
      new Endpoint[El, KIn, VIn, ROut, F, Out](extractor) {
        def apply(in: VIn): F[Out] = fnApply(in, f)
      }
  }

  def apply[H <: HList, Fold, El <: HList, KIn <: HList, VIn <: HList, ROut, Fn, Out](apiList: ApiTypeCarrier[H])
                                                                                 (implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil, HNil), Fold],
                                                                                           ev: FoldResultEvidence.Aux[Fold, El, KIn, VIn, Out],
                                                                                           extractor: RouteExtractor.Aux[El, KIn, VIn, HNil, ROut],
                                                                                           fnApply: Lazy[FunctionApply.Aux[VIn, Fn, F, Out]]): Derivation[El, KIn, VIn, ROut, Fn, Out] = new Derivation[El, KIn, VIn, ROut, Fn, Out](extractor, fnApply.value)
}
