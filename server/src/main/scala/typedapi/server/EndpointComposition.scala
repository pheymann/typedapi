package typedapi.server

import shapeless._

import scala.language.higherKinds

sealed trait PrecompileEndpoint[H <: HList] {

  type Comp[_[_]] <: HList
  type Out[_[_]]  <: HList

  def precompiled[F[_]]: Out[F]
}

object PrecompileEndpoint {

  type Aux[H <: HList, Comp0[_[_]] <: HList, Out0[_[_]] <: HList] = PrecompileEndpoint[H] {
    type Comp[F[_]] = Comp0[F]
    type Out[F[_]]  = Out0[F]
  }
}

trait EndpointConstructor[F[_], Fun, El <: HList, In <: HList, ROut, CIn <: HList, Out] {

  def apply(fun: Fun): Endpoint[El, In, ROut, CIn, F, Out]
}

trait PrecompileEndpointLowPrio {

  implicit val hnilPrecompiledCase = new PrecompileEndpoint[HNil] {
    type Comp[_[_]] = HNil
    type Out[_[_]]  = HNil

    def precompiled[F[_]] = HNil
  }

  final class Precompiled[El <: HList, In <: HList, CIn <: HList, Fun[_[_]], Out0, ROut, CompT[_[_]] <: HList, OutT[_[_]] <: HList, T <: HList](val extractor: RouteExtractor.Aux[El, In, HNil, ROut], 
                                                                                                                                          val funApply: FunctionApply.Aux[In, CIn, Fun, Out0], 
                                                                                                                                          val next: PrecompileEndpoint.Aux[T, CompT, OutT]) extends PrecompileEndpoint[(El, In, Out0) :: T] {
    type Comp[F[_]] = Fun[F] :: CompT[F]
    type Out[F[_]]  = EndpointConstructor[F, Fun[F], El, In, ROut, CIn, Out0] :: OutT[F]

    def constructor[F[_]] = new EndpointConstructor[F, Fun[F], El, In, ROut, CIn, Out0] {
      def apply(fun: Fun[F]): Endpoint[El, In, ROut, CIn, F, Out0] = new Endpoint[El, In, ROut, CIn, F, Out0](extractor) {
        def apply(in: CIn): F[Out0] = funApply(in, fun)
      }
    }

    def precompiled[F[_]] = constructor[F] :: next.precompiled[F]
  }

  implicit def precompiledCase[El <: HList, In <: HList, Out, ROut, T <: HList](implicit extractor: RouteExtractor.Aux[El, In, HNil, ROut], 
                                                                                         funApply: FunctionApply[In, Out], 
                                                                                         next: PrecompileEndpoint[T]) = 
    new Precompiled[El, In, funApply.CIn, funApply.Fun, Out, ROut, next.Comp, next.Out, T](extractor, funApply, next)
}

final case class FunctionComposition[Comp <: HList](funs: Comp) {

  def :|:[Fun](fun: Fun): FunctionComposition[Fun :: Comp] = FunctionComposition(fun :: funs)
}

object =: {

  def :|:[Fun](fun: Fun): FunctionComposition[Fun :: HNil] = FunctionComposition(fun :: HNil)
}

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

          def apply(req: executor.R, eReq: EndpointRequest): Option[executor.Out] = executor(req, eReq, endpoint)
        } :: next(constructors.tail, fun.tail)
    }
}

final class EndpointCompositionDefinition[H <: HList, Comp[_[_]] <: HList, Pre[_[_]] <: HList](pre: PrecompileEndpoint.Aux[H, Comp, Pre]) {

  def to[F[_]](comp: FunctionComposition[Comp[F]])(implicit merge: MergeToEndpoint[F, Pre[F], Comp[F]]): merge.Out =
    merge(pre.precompiled[F], comp.funs)
}
