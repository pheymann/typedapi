package typedapi.shared

import shapeless.HList

sealed trait FoldResultEvidence[Fold] {

  type El  <: HList
  type KIn <: HList
  type VIn <: HList
  type Out
}

object FoldResultEvidence {

  type Aux[Fold, El0 <: HList, KIn0 <: HList, VIn0 <: HList, Out0] = FoldResultEvidence[Fold] {
    type El  = El0
    type KIn = KIn0
    type VIn = VIn0
    type Out = Out0
  }
}

trait FoldResultEvidenceLowPrio {

  implicit def evidence[El0 <: HList, KIn0 <: HList, VIn0 <: HList, Out0]: FoldResultEvidence.Aux[(El0, KIn0, VIn0, Out0), El0, KIn0, VIn0, Out0] = 
    new FoldResultEvidence[(El0, KIn0, VIn0, Out0)] {
      type El  = El0
      type KIn = KIn0
      type VIn = VIn0
      type Out = Out0
    }
}
