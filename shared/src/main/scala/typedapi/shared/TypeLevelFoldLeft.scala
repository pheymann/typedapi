package typedapi.shared

import shapeless._

import scala.annotation.implicitNotFound

// INTERNAL API

/** Reimplements shapeles Case2 but on the type level (no real HList instance). */
@implicitNotFound("""Woops, you shouldn't be here. We cannot find TypeLevelFoldFunction instance.

input: ${In}
aggregation: ${Agg}""")
sealed trait TypeLevelFoldFunction[In, Agg] {

  type Out
}

object TypeLevelFoldFunction {

  type Aux[In, Agg, Out0] = TypeLevelFoldFunction[In, Agg] { type Out = Out0 }

  def at[In, Agg, Out0]: Aux[In, Agg, Out0] = new TypeLevelFoldFunction[In, Agg] {
    type Out = Out0
  }
}

/** Reimplements shapeless LeftFolder but on the type level (no real HList instance) */
@implicitNotFound("""Woops, you shouldn't be here. We cannot find TypeLevelFold instance.

list: ${H}
aggregation: ${Agg}""")
sealed trait TypeLevelFoldLeft[H <: HList, Agg] extends Serializable {

  type Out
}

object TypeLevelFoldLeft {

  type Aux[H <: HList, Agg, Out0] = TypeLevelFoldLeft[H, Agg] {
    type Out = Out0
  }
}

trait TypeLevelFoldLeftLowPrio {

  implicit def hnilCase[Agg]: TypeLevelFoldLeft.Aux[HNil, Agg, Agg] = new TypeLevelFoldLeft[HNil, Agg] {
    type Out = Agg
  }

  implicit def foldCase[H, T <: HList, Agg, FtOut, FOut](implicit f: TypeLevelFoldFunction.Aux[H, Agg, FtOut], 
                                                                  next: Lazy[TypeLevelFoldLeft.Aux[T, FtOut, FOut]]): TypeLevelFoldLeft.Aux[H :: T, Agg, FOut] = new TypeLevelFoldLeft[H :: T, Agg] {
    type Out = next.value.Out
  }
}

/** Helper to work on a composition of HLists we want to fold over. */
@implicitNotFound("""Woops, you shouldn't be here. We cannot find TypeLevelFoldList instance.

apis: ${H}""")
trait TypeLevelFoldLeftList[H <: HList] {

  type Out <: HList
}

object TypeLevelFoldLeftList {

  type Aux[H <: HList, Out0 <: HList] = TypeLevelFoldLeftList[H] {
    type Out = Out0
  }
}

trait TypeLevelFoldLeftListLowPrio {

  implicit def lastFoldLeftList[H <: HList, Agg](implicit folder0: TypeLevelFoldLeft[H, Agg]) = new TypeLevelFoldLeftList[H :: HNil] {
    type Out = folder0.Out :: HNil
  }

  implicit def folderLeftList[H <: HList, Agg, T <: HList](implicit folder0: TypeLevelFoldLeft[H, Agg], list: TypeLevelFoldLeftList[T]) = new TypeLevelFoldLeftList[H :: T] {
    type Out = folder0.Out :: list.Out
  }
}
