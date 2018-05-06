package typedapi.server

import typedapi.shared._
import shapeless._
import shapeless.ops.hlist.Tupler
import shapeless.ops.function._

import scala.language.higherKinds
import scala.annotation.implicitNotFound

/** Fuses RouteExtractor, FunApply and endpoint function fun into an Endpoint. */
trait EndpointConstructor[F[_], Fn, El <: HList, KIn <: HList, VIn <: HList, ROut, Out] {

  def apply(fn: Fn): Endpoint[El, KIn, VIn, ROut, F, Out]
}

/** Compiles RouteExtractor and FunApply for every API endpoint and generates expected list of endpoint functions. */
@implicitNotFound("""Could not precompile your API. This can happen when:
  - you try to extract an value from the route which is not supported (ValueExtractor in RouteExtractor.scala)
 
transformed: ${H}""")
sealed trait PrecompileEndpoint[F[_], H <: HList] {

  // list of expected endpoint functions
  type Fns <: HList
  // list of endpoint constructors
  type Consts <: HList

  def constructors: Consts
}

object PrecompileEndpoint extends PrecompileEndpointLowPrio {

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

  implicit def constructorsCase[F[_], Fn, El <: HList, KIn <: HList, VIn <: HList, Out, ROut, T <: HList]
    (implicit extractor: RouteExtractor.Aux[El, KIn, VIn, HNil, ROut],
              vinToFn: FnFromProduct.Aux[VIn => F[Out], Fn],
              fnToVIn: Lazy[FnToProduct.Aux[Fn, VIn => F[Out]]],
              next: PrecompileEndpoint[F, T]) =
    new PrecompileEndpoint[F, (El, KIn, VIn, Out) :: T] {
      type Fns    = Fn :: next.Fns
      type Consts = EndpointConstructor[F, Fn, El, KIn, VIn, ROut, Out] :: next.Consts

      val constructor = new EndpointConstructor[F, Fn, El, KIn, VIn, ROut, Out] {
        def apply(fn: Fn): Endpoint[El, KIn, VIn, ROut, F, Out] = new Endpoint[El, KIn, VIn, ROut, F, Out](extractor) {
          private val fin = fnToVIn.value(fn)

          def apply(in: VIn): F[Out] = fin(in)
        }
      }

      val constructors = constructor :: next.constructors
    }
}

@implicitNotFound("""Whoops, you should not be here. This seems to be a bug.

constructors: ${Consts}
functions: ${Fns}""")
sealed trait MergeToEndpoint[F[_], Consts <: HList, Fns <: HList] {

  type Out <: HList

  def apply(constructors: Consts, fns: Fns): Out
}

object MergeToEndpoint extends MergeToEndpointLowPrio {

  type Aux[F[_], Consts <: HList, Fns <: HList, Out0 <: HList] = MergeToEndpoint[F, Consts, Fns] { type Out = Out0 }
}

trait MergeToEndpointLowPrio {

  implicit def hnilMergeCase[F[_]] = new MergeToEndpoint[F, HNil, HNil] {
    type Out = HNil

    def apply(constructors: HNil, fns: HNil): Out = HNil
  }

  implicit def mergeCase[F[_], El <: HList, KIn <: HList, VIn <: HList, Out0, ROut, Consts <: HList, Fn, Fns <: HList]
    (implicit next: MergeToEndpoint[F, Consts, Fns]) =
    new MergeToEndpoint[F, EndpointConstructor[F, Fn, El, KIn, VIn, ROut, Out0] :: Consts, Fn :: Fns] {
      type Out = Endpoint[El, KIn, VIn, ROut, F, Out0] :: next.Out

      def apply(constructors: EndpointConstructor[F, Fn, El, KIn, VIn, ROut, Out0] :: Consts, fns: Fn :: Fns): Out = {
        val endpoint = constructors.head(fns.head)

        endpoint :: next(constructors.tail, fns.tail)
      }
    }
}

final class ExecutableCompositionDerivation[F[_]] {

  final class Derivation[H <: HList, Fns <: HList, FnsTup, Consts <: HList](pre: PrecompileEndpoint.Aux[F, H, Fns, Consts], 
                                                                            gen: Generic.Aux[FnsTup, Fns]) {

    /** Restricts type of input parameter to a composition of functions defined by the precompile-stage.
      *
      * {{{
      * val Api =
      *   (:= :> Segment[String]('name) :> Get[User]) :|:
      *   (:= :> "foo" :> Segment[String]('name) :> Get[User])
      * 
      * val f0: String => IO[User] = name => IO.pure(User(name))
      * val f1: String => IO[User] = name => IO.pure(User(name))
      * deriveAll[IO](Api).from(f0 _ :|: f1 _ :|: =:)
      * }}}
      */
    def from(fns: FnsTup)(implicit merge: MergeToEndpoint[F, Consts, Fns]): merge.Out =
      merge(pre.constructors, gen.to(fns))
  }

  def apply[H <: HList, Fold <: HList, Fns <: HList, FnsTup, Consts <: HList](apiLists: CompositionCons[H])
                                      (implicit folder: TypeLevelFoldLeftList.Aux[H, Fold],
                                                pre: PrecompileEndpoint.Aux[F, Fold, Fns, Consts],
                                                fnsTupled: Tupler.Aux[Fns, FnsTup],
                                                gen: Generic.Aux[FnsTup, Fns]): Derivation[Fold, Fns, FnsTup, Consts] =
    new Derivation[Fold, Fns, FnsTup, Consts](pre, gen)
}
