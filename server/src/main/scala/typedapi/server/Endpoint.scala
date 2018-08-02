package typedapi.server

import typedapi.shared._
import shapeless._
import shapeless.labelled.FieldType
import shapeless.ops.function._

import scala.language.higherKinds

/** Represents a server endpoint and is basically a function which gets the expected input `VIn` and returns the expected output. */
abstract class Endpoint[El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, ROut, F[_], Out]
    (val method: String, val extractor: RouteExtractor.Aux[El, KIn, VIn, M, HNil, ROut], val headers: Map[String, String]) {

  def apply(in: VIn): F[Out]
}

/** Request representation which every server implementation has to provide. */
final case class EndpointRequest(method: String, 
                                 uri: List[String],
                                 queries: Map[String, List[String]],
                                 headers: Map[String, String])

final class ExecutableDerivation[F[_]] {

  final class Derivation[El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, ROut, Fn, Out]
    (extractor: RouteExtractor.Aux[El, KIn, VIn, M, HNil, ROut],
     method: String,
     headers: Map[String, String],
     fnToVIn: FnToProduct.Aux[Fn, VIn => F[Out]]) {

    /** Restricts type of parameter `fn` to a function defined by the given API:
      * 
      * {{{
      * val Api = := :> Segment[String]('name) :> Get[User]
      * 
      * derive[IO](Api).from(name: String => IO.pure(User(name)))
      * }}}
      */
    def from(fn: Fn): Endpoint[El, KIn, VIn, M, ROut, F, Out] =
      new Endpoint[El, KIn, VIn, M, ROut, F, Out](method, extractor, headers) {
        private val fin = fnToVIn(fn)

        def apply(in: VIn): F[Out] = fin(in)
      }
  }

  def apply[H <: HList, El <: HList, KIn <: HList, VIn <: HList, ROut, Fn, M <: MethodType, MT <: MediaType, Out]
    (apiList: ApiTypeCarrier[H])
    (implicit folder: Lazy[TypeLevelFoldLeft.Aux[H, Unit, (El, KIn, VIn, M, FieldType[MT, Out])]],
              extractor: RouteExtractor.Aux[El, KIn, VIn, M, HNil, ROut],
              methodShow: MethodToString[M],
              serverHeaders: ServerHeaderExtractor[El],
              inToFn: Lazy[FnFromProduct.Aux[VIn => F[Out], Fn]],
              fnToVIn: Lazy[FnToProduct.Aux[Fn, VIn => F[Out]]]): Derivation[El, KIn, VIn, M, ROut, Fn, Out] =
    new Derivation[El, KIn, VIn, M, ROut, Fn, Out](extractor, methodShow.show, serverHeaders(Map.empty), fnToVIn.value)
}
