package typedapi.shared

import shapeless.HList

sealed trait FoldResultEvidence[Fold] {

  type El <: HList
  type In <: HList
  type Out
}

object FoldResultEvidence {

  type Aux[Fold, El0 <: HList, In0 <: HList, Out0] = FoldResultEvidence[Fold] {
    type El = El0
    type In = In0
    type Out = Out0
  }
}

trait FoldResultEvidenceLowPrio {

  implicit def evidence[El0 <: HList, In0 <: HList, Out0]: FoldResultEvidence.Aux[(El0, In0, Out0), El0, In0, Out0] = new FoldResultEvidence[(El0, In0, Out0)] {
    type El = El0
    type In = In0
    type Out = Out0
  }
}
