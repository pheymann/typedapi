package typedapi.server

import typedapi.shared._
import shapeless._
import shapeless.ops.function._

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
                                                                                 fnToVIn: FnToProduct.Aux[Fn, VIn => F[Out]]) {

    /** Restricts type of parameter `fn` to a function defined by the given API:
      * 
      * {{{
      * val Api = := :> Segment[String]('name) :> Get[User]
      * 
      * derive[IO](Api).from(name: String => IO.pure(User(name)))
      * }}}
      */
    def from(fn: Fn): Endpoint[El, KIn, VIn, ROut, F, Out] =
      new Endpoint[El, KIn, VIn, ROut, F, Out](extractor) {
        private val fin = fnToVIn(fn)

        def apply(in: VIn): F[Out] = fin(in)
      }
  }

  def apply[H <: HList, El <: HList, KIn <: HList, VIn <: HList, ROut, Fn, Out](apiList: ApiTypeCarrier[H])
                                                                               (implicit folder: Lazy[TypeLevelFoldLeft.Aux[H, (HNil, HNil, HNil), (El, KIn, VIn, Out)]],
                                                                                         extractor: RouteExtractor.Aux[El, KIn, VIn, HNil, ROut],
                                                                                         inToFn: Lazy[FnFromProduct.Aux[VIn => F[Out], Fn]],
                                                                                         fnToVIn: Lazy[FnToProduct.Aux[Fn, VIn => F[Out]]]): Derivation[El, KIn, VIn, ROut, Fn, Out] = 
    new Derivation[El, KIn, VIn, ROut, Fn, Out](extractor, fnToVIn.value)
}
