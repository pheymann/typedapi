package typedapi.server

import typedapi.shared._
import shapeless._
import shapeless.labelled.FieldType
import shapeless.ops.function._

import scala.language.higherKinds
import scala.annotation.implicitNotFound

/** Fuses [[RouteExtractor]] and the endpoint function into an [[Endpoint]]. */
trait EndpointConstructor[F[_], Fn, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, ROut, Out] {

  def apply(fn: Fn): Endpoint[El, KIn, VIn, M, ROut, F, Out]
}

/** Compiles RouteExtractor and FunApply for every API endpoint and generates expected list of endpoint functions. */
@implicitNotFound("""Could not precompile your API. This can happen when you try to extract an value from the route which is not supported (ValueExtractor in RouteExtractor.scala)
 
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
    type Fns    = Fns0
    type Consts = Consts0
  }
}

trait PrecompileEndpointLowPrio {

  implicit def hnilPrecompiledCase[F[_]] = new PrecompileEndpoint[F, HNil] {
    type Fns    = HNil
    type Consts = HNil

    val constructors = HNil
  }

  implicit def constructorsCase[F[_], Fn, El <: HList, KIn <: HList, VIn <: HList, MT <: MediaType, Out, M <: MethodType, ROut, T <: HList]
    (implicit extractor: RouteExtractor.Aux[El, KIn, VIn, M, HNil, ROut],
              methodShow: MethodToString[M],
              serverHeaders: ServerHeaderExtractor[El],
              vinToFn: FnFromProduct.Aux[VIn => F[Result[Out]], Fn],
              fnToVIn: Lazy[FnToProduct.Aux[Fn, VIn => F[Result[Out]]]],
              next: PrecompileEndpoint[F, T]) =
    new PrecompileEndpoint[F, (El, KIn, VIn, M, FieldType[MT, Out]) :: T] {
      type Fns    = Fn :: next.Fns
      type Consts = EndpointConstructor[F, Fn, El, KIn, VIn, M, ROut, Out] :: next.Consts

      val constructor = new EndpointConstructor[F, Fn, El, KIn, VIn, M, ROut, Out] {
        def apply(fn: Fn): Endpoint[El, KIn, VIn, M, ROut, F, Out] = new Endpoint[El, KIn, VIn, M, ROut, F, Out](methodShow.show, extractor, serverHeaders(Map.empty)) {
          private val fin = fnToVIn.value(fn)

          def apply(in: VIn): F[Result[Out]] = fin(in)
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

  implicit def mergeCase[F[_], El <: HList, KIn <: HList, VIn <: HList, Out0, M <: MethodType, ROut, Consts <: HList, Fn, Fns <: HList]
    (implicit next: MergeToEndpoint[F, Consts, Fns]) =
    new MergeToEndpoint[F, EndpointConstructor[F, Fn, El, KIn, VIn, M, ROut, Out0] :: Consts, Fn :: Fns] {
      type Out = Endpoint[El, KIn, VIn, M, ROut, F, Out0] :: next.Out

      def apply(constructors: EndpointConstructor[F, Fn, El, KIn, VIn, M, ROut, Out0] :: Consts, fns: Fn :: Fns): Out = {
        val endpoint = constructors.head(fns.head)

        endpoint :: next(constructors.tail, fns.tail)
      }
    }
}

final class ExecutableCompositionDerivation[F[_]] {

  final class Derivation[H <: HList, Fns <: HList, Consts <: HList, Out <: HList, Drv](pre: PrecompileEndpoint.Aux[F, H, Fns, Consts], 
                                                                                       merge: MergeToEndpoint.Aux[F, Consts, Fns, Out],
                                                                                       derived: FnFromProduct.Aux[Fns => Out, Drv]) {

    /** Restricts type of input parameter to a composition of functions defined by the precompile-stage.
      *
      * {{{
      * val Api =
      *   (:= :> Segment[String]('name) :> Get[User]) :|:
      *   (:= :> "foo" :> Segment[String]('name) :> Get[User])
      * 
      * val f0: String => IO[User] = name => IO.pure(User(name))
      * val f1: String => IO[User] = name => IO.pure(User(name))
      * deriveAll[IO](Api).from(f0 _, f1 _)
      * }}}
      */
    val from: Drv = derived.apply(fns => merge(pre.constructors, fns))
  }

  def apply[H <: HList, FH <: HList, Fold <: HList, Fns <: HList, FnsTup, Consts <: HList, Out <: HList, Drv](apiLists: CompositionCons[H])
                                      (implicit filter: TplRightFolder.Aux[FilterClientElements.type, H, HNil, FH],
                                                folders: Lazy[TplRightFolder.Aux[ApiTransformer.type, FH, HNil, Fold]],
                                                pre: PrecompileEndpoint.Aux[F, Fold, Fns, Consts],
                                                merge: MergeToEndpoint.Aux[F, Consts, Fns, Out],
                                                derived: FnFromProduct.Aux[Fns => Out, Drv]): Derivation[Fold, Fns, Consts, Out, Drv] =
    new Derivation[Fold, Fns, Consts, Out, Drv](pre, merge, derived)
}
