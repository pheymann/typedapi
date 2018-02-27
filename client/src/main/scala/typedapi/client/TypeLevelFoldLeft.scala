package typedapi.client

import shapeless._

import scala.annotation.implicitNotFound

/** Reimplements shapeles Case2 but on the type level (no real HList instance) */
@implicitNotFound("Cannot find TypeLevelFoldFunction instance for input = ${In}")
sealed trait TypeLevelFoldFunction[H, In] extends Serializable {

  type Out

  def apply(agg: In): Out
}

object TypeLevelFoldFunction {

  type Aux[H, In, Out0] = TypeLevelFoldFunction[H, In] { type Out = Out0 }

  final class FoldFunctionHelper[H, In] {

    def apply[Out0](f: In => Out0): Aux[H, In, Out0] = new TypeLevelFoldFunction[H, In] {
      type Out = Out0

      def apply(agg: In) = f(agg)
    }
  }

  def at[H, In] = new FoldFunctionHelper[H, In]
}

/** Reimplements shapeless LeftFolder but on the type level (no real HList instance) */
@implicitNotFound("Cannot find TypeLevelFold instance for:\n - list = ${H}\n - aggregation = ${Agg}")
sealed trait TypeLevelFoldLeft[H <: HList, Agg] extends Serializable {

  type Out

  def apply(agg: Agg): Out
}

object TypeLevelFoldLeft {

  type Aux[H <: HList, Agg, Out0] = TypeLevelFoldLeft[H, Agg] {
    type Out = Out0
  }
}

trait TypeLevelFoldLeftLowPrio {

  implicit def hnilCase[Agg]: TypeLevelFoldLeft.Aux[HNil, Agg, Agg] = new TypeLevelFoldLeft[HNil, Agg] {
    type Out = Agg

    def apply(agg: Agg): Out = agg
  }

  implicit def foldCase[H, T <: HList, Agg, FtOut, FOut](implicit f: TypeLevelFoldFunction.Aux[H, Agg, FtOut], 
                                                                  next: Lazy[TypeLevelFoldLeft.Aux[T, FtOut, FOut]]): TypeLevelFoldLeft.Aux[H :: T, Agg, FOut] = new TypeLevelFoldLeft[H :: T, Agg] {
    type Out = FOut

    def apply(agg: Agg): Out = next.value(f(agg))
  }
}

/** Helper to work on a composition of HLists we want to fold over. */
trait TypeLevelFoldLeftList[H <: HList] {

  type In  <: HList
  type Out <: HList
}

object TypeLevelFoldLeftList {

  type Aux[H <: HList, In0 <: HList, Out0 <: HList] = TypeLevelFoldLeftList[H] {
    type In  = In0
    type Out = Out0
  }
}

trait TypeLevelFoldLeftListLowPrio {

  implicit def lastFoldLeftList[Api <: HList](implicit folder0: Folder[Api]) = new TypeLevelFoldLeftList[Api :: HNil] {
    type In  = Api
    type Out = folder0.Out :: HNil
  }

  implicit def folderLeftList[Api <: HList, Agg, T <: HList](implicit folder0: TypeLevelFoldLeft[Api, Agg], list: TypeLevelFoldLeftList[T]) = new TypeLevelFoldLeftList[Api :: T] {
    type In  = Api
    type Out = folder0.Out :: list.Out
  }
}
