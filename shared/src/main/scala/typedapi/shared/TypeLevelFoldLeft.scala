package typedapi.shared

import shapeless._

import scala.annotation.implicitNotFound

// INTERNAL API

@implicitNotFound("We cannot find a Case0 for poly ${P}.")
trait TplCase0[P] { type Result }

object TplCase0 {

  type Aux[P, Result0] = TplCase0[P] { type Result = Result0 }
}

/** Reimplements shapeles Poly0 but on the type level (no real HList instance). */
trait TplPoly0 {

  def at[Result0] = new TplCase0[this.type] { type Result = Result0 }
}

@implicitNotFound("We cannot find a Case2(${In0}, ${In1}) for poly ${P}.")
trait TplCase2[P, In0, In1] { type Result }

object TplCase2 {

  type Aux[P, In0, In1, Result0] = TplCase2[P, In0, In1] { type Result = Result0 }
}

/** Reimplements shapeles Poly2 but on the type level (no real HList instance). */
trait TplPoly2 {

  def at[In0, In1, Result0] = new TplCase2[this.type, In0, In1] { type Result = Result0 }
}

/** Reimplements shapeles LeftFolder but on the type level (no real HList instance). */
@implicitNotFound("""Woops, you shouldn't be here. We cannot find LeftFolder instance.

poly: ${P}
hlist: ${H}
input: ${In}""")
sealed trait TplLeftFolder[P, H <: HList, In] { type Out }

sealed trait TplLeftFolderLowPrio {

  implicit def tplLeftFolderHNil[P, In]: TplLeftFolder.Aux[P, HNil, In, In] = new TplLeftFolder[P, HNil, In] {
    type Out = In
  }

  implicit def tplLeftFolderCase[P, H, T <: HList, In, Result, Out0](implicit f: TplCase2.Aux[P, H, In, Result], 
                                                                              next: Lazy[TplLeftFolder.Aux[P, T, Result, Out0]]): TplLeftFolder.Aux[P, H :: T, In, Out0] = 
    new TplLeftFolder[P, H :: T, In] {
      type Out = Out0
    }
}

object TplLeftFolder extends TplLeftFolderLowPrio {

  type Aux[P, H <: HList, In,  Out0] = TplLeftFolder[P, H, In] { type Out = Out0 }
}


/** Implements filter of type-level HLists. */
@implicitNotFound("""Woops, you shouldn't be here. We cannot find TplFilter instance.

poly: ${P}
hlist: ${H}""")
sealed trait TplFilter[P, H <: HList] { type Out <: HList }

sealed trait TplFilterLowPrio {

  implicit def tplFilterHNil[P]: TplFilter.Aux[P, HNil, HNil] = new TplFilter[P, HNil] {
    type Out = HNil
  }

  implicit def tplKeepCase[P, H, T <: HList, Out0 <: HList](implicit next: TplFilter.Aux[P, T, Out0]): TplFilter.Aux[P, H :: T, H :: Out0] =
    new TplFilter[P, H :: T] {
      type Out = H :: Out0
    }

  implicit def tplFilterHList[P, H <: HList, T <: HList, Result <: HList, Out0 <: HList]
    (implicit filter: Lazy[TplFilter.Aux[P, H, Result]],
              next: Lazy[TplFilter.Aux[P, T, Out0]]): TplFilter.Aux[P, H :: T, Result :: Out0] =
    new TplFilter[P, H :: T] {
      type Out = Result :: Out0
    }
}

object TplFilter extends TplFilterLowPrio {

  type Aux[P, H <: HList, Out0 <: HList] = TplFilter[P, H] { type Out = Out0 }

  implicit def tplFilterCase[P, H, T <: HList, Out0 <: HList]
    (implicit f: TplCase0.Aux[P, H],
              next: Lazy[TplFilter.Aux[P, T, Out0]]): TplFilter.Aux[P, H :: T, Out0] =
    new TplFilter[P, H :: T] {
      type Out = Out0
    }
}

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
