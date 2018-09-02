package typedapi.shared

import shapeless._
import shapeless.ops.hlist.Reverse

import scala.annotation.implicitNotFound

// INTERNAL API

@implicitNotFound("We cannot find a Case2(${In0}, ${In1}) for poly ${P}.")
trait TplCase2[P, In0, In1] { type Result }

object TplCase2 {

  type Aux[P, In0, In1, Result0] = TplCase2[P, In0, In1] { type Result = Result0 }
}

/** Reimplements shapeles Poly2 but on the type level (no real HList instance). */
trait TplPoly2 {

  def at[In0, In1, Result0] = new TplCase2[this.type, In0, In1] { type Result = Result0 }
}

/** Reimplements shapeles TplLeftFolder but on the type level (no real HList instance). */
@implicitNotFound("""Woops, you shouldn't be here. We cannot find TplLeftFolder instance.

poly: ${P}
hlist: ${H}
input: ${In}""")
sealed trait TplLeftFolder[P, H <: HList, In] { type Out }

sealed trait TplLeftFolderLowPrio {

  implicit def tplLeftFolderHNil[P, In]: TplLeftFolder.Aux[P, HNil, In, In] = new TplLeftFolder[P, HNil, In] {
    type Out = In
  }

  implicit def tplLeftFolderCase[P, H, T <: HList, In, Result, Out0]
    (implicit f: TplCase2.Aux[P, H, In, Result],
              next: Lazy[TplLeftFolder.Aux[P, T, Result, Out0]]): TplLeftFolder.Aux[P, H :: T, In, Out0] =
    new TplLeftFolder[P, H :: T, In] {
      type Out = Out0
    }
}

object TplLeftFolder extends TplLeftFolderLowPrio {

  type Aux[P, H <: HList, In,  Out0] = TplLeftFolder[P, H, In] { type Out = Out0 }

  implicit def leftFolderHList[P, H <: HList, T <: HList, In <: HList, Result, Out0 <: HList]
    (implicit folder: TplLeftFolder.Aux[P, H, Unit, Result],
              next: Lazy[TplLeftFolder.Aux[P, T, Result :: In, Out0]]): Aux[P, H :: T, In, Out0] =
    new TplLeftFolder[P, H :: T, In] {
      type Out = Out0
    }
}

/** Reimplements shapeles RightFolder but on the type level (no real HList instance). */
@implicitNotFound("""Woops, you shouldn't be here. We cannot find RightFolder instance.

poly: ${P}
hlist: ${H}
input: ${In}""")
sealed trait TplRightFolder[P, H <: HList, In] { type Out }

sealed trait TplRightFolderLowPrio {

  implicit def rightFolderUseLeft[P, H <: HList, In, Out <: HList, ROut <: HList]
    (implicit folder: TplLeftFolder.Aux[P, H, In, Out], reverse: Reverse.Aux[Out, ROut]): TplRightFolder.Aux[P, H, In, ROut] = new TplRightFolder[P, H, In] {
    type Out = ROut
  }
}

object TplRightFolder extends TplLeftFolderLowPrio with TplRightFolderLowPrio {

  type Aux[P, H <: HList, In,  Out0] = TplRightFolder[P, H, In] { type Out = Out0 }

  implicit def rightFolderHList[P, H <: HList, T <: HList, In <: HList, Result, Out0 <: HList]
    (implicit folder: TplRightFolder.Aux[P, H, In, Result],
              next: TplRightFolder.Aux[P, T, In, Out0]): Aux[P, H :: T, In, Result :: Out0] =
    new TplRightFolder[P, H :: T, In] {
      type Out = Result :: Out0
    }
}
