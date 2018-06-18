package typedapi.client

import typedapi.shared.MethodCall
import shapeless._
import shapeless.ops.function.FnFromProduct

/** Derives executables from list of RequestBuilders. */
sealed trait ExecutablesFromHList[H <: HList] {

  type Out <: HList

  def apply(h: H): Out
}

object ExecutablesFromHList extends ExecutablesFromHListLowPrio {

  type Aux[H <: HList, Out0 <: HList] = ExecutablesFromHList[H] { type Out = Out0 }
}

trait ExecutablesFromHListLowPrio {

  implicit val noExecutable = new ExecutablesFromHList[HNil] {
    type Out = HNil

    def apply(h: HNil): Out = HNil
  }

  implicit def deriveExcutable[El <: HList, KIn <: HList, VIn <: HList, M <: MethodCall, O, D <: HList, T <: HList](implicit next: ExecutablesFromHList[T],
                                                                                                                             vinToFn: FnFromProduct[VIn => ExecutableDerivation[El, KIn, VIn, M, O, D]]) = 
    new ExecutablesFromHList[RequestDataBuilder.Aux[El, KIn, VIn, M, O, D] :: T] {
      type Out = vinToFn.Out :: next.Out

      def apply(comps: RequestDataBuilder.Aux[El, KIn, VIn, M, O, D] :: T): Out = {
        val fn = vinToFn.apply(input => new ExecutableDerivation[El, KIn, VIn, M, O, D](comps.head, input))

        fn :: next(comps.tail)
      }
    }
}

