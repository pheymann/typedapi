package typedapi.server

import typedapi.shared._
import shapeless._

import scala.language.higherKinds
import scala.annotation.implicitNotFound

/** Fuses RouteExtractor, FunApply and endpoint function fun into an Endpoint. */
trait EndpointConstructor[F[_], Fn, El <: HList, KIn <: HList, VIn <: HList, ROut, Out] {

  def apply(fn: Fn): Endpoint[El, KIn, VIn, ROut, F, Out]
}

/** Compiles RouteExtractor and FunApply for every API endpoint and generates expected list of endpoint functions. */
@implicitNotFound("""Could not precompile your API. This can happen when:
  - you defined an endpoint function with an arity larger than the biggest supported one (FunctionApply.scala)
  - you try to extract an value from the route which is not supported (ValueExtractor in RouteExtractor.scala)
 
transformed: ${H}""")
sealed trait PrecompileEndpoint[F[_], H <: HList] {

  // list of expected endpoint functions
  type Fns <: HList
  // list of endpoint constructors
  type Consts <: HList

  def constructors: Consts
}

object PrecompileEndpoint {

  type Aux[F[_], H <: HList, Fns0 <: HList, Consts0 <: HList] = PrecompileEndpoint[F, H] {
    type Fns   = Fns0
    type Consts = Consts0
  }
}

trait PrecompileEndpointLowPrio {

  implicit def hnilPrecompiledCase[F[_]] = new PrecompileEndpoint[F, HNil] {
    type Fns    = HNil
    type Consts = HNil

    val constructors = HNil
  }

  implicit def constructorsCase[F[_], El <: HList, KIn <: HList, VIn <: HList, Out, ROut, T <: HList](implicit extractor: RouteExtractor.Aux[El, KIn, VIn, HNil, ROut], 
                                                                                                              fnApply: FunctionApply[VIn, F, Out], 
                                                                                                              next: PrecompileEndpoint[F, T]) = new PrecompileEndpoint[F, (El, KIn, VIn, Out) :: T] {
    type Fn    = fnApply.Fn
    type Fns   = Fn :: next.Fns
    type Consts = EndpointConstructor[F, Fn, El, KIn, VIn, ROut, Out] :: next.Consts

    val constructor = new EndpointConstructor[F, Fn, El, KIn, VIn, ROut, Out] {
      def apply(fn: Fn): Endpoint[El, KIn, VIn, ROut, F, Out] = new Endpoint[El, KIn, VIn, ROut, F, Out](extractor) {
        def apply(in: VIn): F[Out] = fnApply(in, fn)
      }
    }

    val constructors = constructor :: next.constructors
  }
}

final case class FunctionComposition[Fns <: HList](fns: Fns) {

  def :|:[Fn](fn: Fn): FunctionComposition[Fn :: Fns] = FunctionComposition(fn :: fns)
}

object =: {

  def :|:[Fn](fn: Fn): FunctionComposition[Fn :: HNil] = FunctionComposition(fn :: HNil)
}

@implicitNotFound("""Could not merge constructors API with your endpoint functions. This can happen when you forget to provide implicits needed by
the executor, e.g. encoders/decoders.

constructors: ${Consts}
functions:   ${Fns}""")
sealed trait MergeToEndpoint[F[_], Consts <: HList, Fns <: HList] {

  type Out <: HList

  def apply(constructors: Consts, fns: Fns): Out
}

object MergeToEndpoint {

  type Aux[F[_], Consts <: HList, Fns <: HList, Out0 <: HList] = MergeToEndpoint[F, Consts, Fns] { type Out = Out0 }
}

trait MergeToEndpointLowPrio {

  implicit def hnilMergeCase[F[_]] = new MergeToEndpoint[F, HNil, HNil] {
    type Out = HNil

    def apply(constructors: HNil, fns: HNil): Out = HNil
  }

  implicit def mergeCase[F[_], El <: HList, KIn <: HList, VIn <: HList, Out0, ROut, Consts <: HList, Fn, Fns <: HList]
    (implicit executor: EndpointExecutor[El, KIn, VIn, ROut, F, Out0], next: MergeToEndpoint[F, Consts, Fns]) =
    new MergeToEndpoint[F, EndpointConstructor[F, Fn, El, KIn, VIn, ROut, Out0] :: Consts, Fn :: Fns] {
      type Out = Serve[executor.R, executor.Out] :: next.Out

      def apply(constructors: EndpointConstructor[F, Fn, El, KIn, VIn, ROut, Out0] :: Consts, fn: Fn :: Fns): Out =
        new Serve[executor.R, executor.Out] {
          private val endpoint = constructors.head(fn.head)

          def apply(req: executor.R, eReq: EndpointRequest): Either[ExtractionError, executor.Out] = executor(req, eReq, endpoint)
        } :: next(constructors.tail, fn.tail)
    }
}

final class ExecutableCompositionDerivation[F[_]] {

  final class Derivation[H <: HList, Fns <: HList, Consts <: HList](pre: PrecompileEndpoint.Aux[F, H, Fns, Consts]) {

    /** Restricts type of input parameter to a composition of functions defined by the precompile.
      *
      * {{{
      * val Api =
      *   (:= :> Segment[String]('name) :> Get[User]) :|:
      *   (:= :> "foo" :> Segment[String]('name) :> Get[User])
      * 
      * val f0: String => IO[User] = name => IO.pure(User(name))
      * val f1: String => IO[User] = name => IO.pure(User(name))
      * link(Api).to[IO](f0 _ :|: f1 _ :|: =:)
      * }}}
      */
    def from(comp: FunctionComposition[Fns])(implicit merge: MergeToEndpoint[F, Consts, Fns]): merge.Out =
      merge(pre.constructors, comp.fns)
  }

  def apply[H <: HList, Fold <: HList](apiLists: CompositionCons[H])
                                      (implicit folder: TypeLevelFoldLeftList.Aux[H, Fold],
                                                pre: PrecompileEndpoint[F, Fold]): Derivation[Fold, pre.Fns, pre.Consts] =
    new Derivation[Fold, pre.Fns, pre.Consts](pre)
}
