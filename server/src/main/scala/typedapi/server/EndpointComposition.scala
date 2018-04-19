package typedapi.server

import shapeless._

import scala.language.higherKinds
import scala.annotation.implicitNotFound

/** Compiles RouteExtractor and FunApply for every API endpoint and generates expected list of endpoint functions. */
@implicitNotFound("""Could not precompile your API. This can happen when:
  - you defined an endpoint function with an arity larger than the biggest supported one (FunctionApply.scala)
  - you try to extract an value from the route which is not supported (ValueExtractor in RouteExtractor.scala)
 
transformed: ${H}""")
sealed trait PrecompileEndpoint[H <: HList] {

  // list of expected endpoint functions
  type Comp[_[_]] <: HList
  // list of endpoint constructors
  type Out[_[_]]  <: HList

  def precompiled[F[_]]: Out[F]
}

object PrecompileEndpoint {

  type Aux[H <: HList, Comp0[_[_]] <: HList, Out0[_[_]] <: HList] = PrecompileEndpoint[H] {
    type Comp[F[_]] = Comp0[F]
    type Out[F[_]]  = Out0[F]
  }
}

/** Fuses RouteExtractor, FunApply and endpoint function fun into an Endpoint. */
trait EndpointConstructor[F[_], Fun, El <: HList, In <: HList, ROut, CIn <: HList, Out] {

  def apply(fun: Fun): Endpoint[El, In, ROut, CIn, F, Out]
}

trait PrecompileEndpointLowPrio {

  implicit val hnilPrecompiledCase = new PrecompileEndpoint[HNil] {
    type Comp[_[_]] = HNil
    type Out[_[_]]  = HNil

    def precompiled[F[_]] = HNil
  }

  final class Precompiled[El <: HList, KIn <: HList, VIn <: HList, Fun[_[_]], Out0, ROut, CompT[_[_]] <: HList, OutT[_[_]] <: HList, T <: HList](val extractor: RouteExtractor.Aux[El, KIn, VIn, HNil, ROut], 
                                                                                                                                                 val funApply: FunctionApply.Aux[VIn, Fun, Out0], 
                                                                                                                                                 val next: PrecompileEndpoint.Aux[T, CompT, OutT]) extends PrecompileEndpoint[(El, KIn, VIn, Out0) :: T] {
    type Comp[F[_]] = Fun[F] :: CompT[F]
    type Out[F[_]]  = EndpointConstructor[F, Fun[F], El, KIn, ROut, VIn, Out0] :: OutT[F]

    def constructor[F[_]] = new EndpointConstructor[F, Fun[F], El, KIn, ROut, VIn, Out0] {
      def apply(fun: Fun[F]): Endpoint[El, KIn, ROut, VIn, F, Out0] = new Endpoint[El, KIn, ROut, VIn, F, Out0](extractor) {
        def apply(in: VIn): F[Out0] = funApply(in, fun)
      }
    }

    def precompiled[F[_]] = constructor[F] :: next.precompiled[F]
  }

  implicit def precompiledCase[El <: HList, KIn <: HList, VIn <: HList, Out, ROut, T <: HList](implicit extractor: RouteExtractor.Aux[El, KIn, VIn, HNil, ROut], 
                                                                                                        funApply: FunctionApply[VIn, Out], 
                                                                                                        next: PrecompileEndpoint[T]) = 
    new Precompiled[El, KIn, VIn, funApply.Fun, Out, ROut, next.Comp, next.Out, T](extractor, funApply, next)
}

final case class FunctionComposition[Comp <: HList](funs: Comp) {

  def :|:[Fun](fun: Fun): FunctionComposition[Fun :: Comp] = FunctionComposition(fun :: funs)
}

object =: {

  def :|:[Fun](fun: Fun): FunctionComposition[Fun :: HNil] = FunctionComposition(fun :: HNil)
}

@implicitNotFound("""Could not merge precompiled API with your endpoint functions. This can happen when you forget to provide implicits needed by
the executor, e.g. encoders/decoders.

precompiled: ${Pre}
functions:   ${Fun}""")
sealed trait MergeToEndpoint[F[_], Pre <: HList, Fun <: HList] {

  type Out <: HList

  def apply(precompiled: Pre, fun: Fun): Out
}

object MergeToEndpoint {

  type Aux[F[_], Pre <: HList, Fun <: HList, Out0 <: HList] = MergeToEndpoint[F, Pre, Fun] { type Out = Out0 }
}

trait MergeToEndpointLowPrio {

  implicit def hnilMergeCase[F[_]] = new MergeToEndpoint[F, HNil, HNil] {
    type Out = HNil

    def apply(precompiled: HNil, fun: HNil): Out = HNil
  }

  implicit def mergeCase[F[_], El <: HList, In <: HList, Out0, ROut, CIn <: HList, PreT <: HList, Fun, FunT <: HList]
    (implicit executor: EndpointExecutor[El, In, ROut, CIn, F, Out0], next: MergeToEndpoint[F, PreT, FunT]) =
    new MergeToEndpoint[F, EndpointConstructor[F, Fun, El, In, ROut, CIn, Out0] :: PreT, Fun :: FunT] {
      type Out = Serve[executor.R, executor.Out] :: next.Out

      def apply(constructors: EndpointConstructor[F, Fun, El, In, ROut, CIn, Out0] :: PreT, fun: Fun :: FunT): Out =
        new Serve[executor.R, executor.Out] {
          private val endpoint = constructors.head(fun.head)

          def apply(req: executor.R, eReq: EndpointRequest): Either[ExtractionError, executor.Out] = executor(req, eReq, endpoint)
        } :: next(constructors.tail, fun.tail)
    }
}

final class EndpointCompositionDefinition[H <: HList, Comp[_[_]] <: HList, Pre[_[_]] <: HList](pre: PrecompileEndpoint.Aux[H, Comp, Pre]) {

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
  def to[F[_]](comp: FunctionComposition[Comp[F]])(implicit merge: MergeToEndpoint[F, Pre[F], Comp[F]]): merge.Out =
    merge(pre.precompiled[F], comp.funs)
}
