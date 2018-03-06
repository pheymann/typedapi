package typedapi.server

import shapeless._
import shapeless.labelled.FieldType

sealed trait EndpointFunction[In <: HList, Out] {

  type Fun
}

object EndpointFunction {

  type Aux[In <: HList, Out, Fun0] = EndpointFunction[In, Out] { type Fun = Fun0 }
}

trait EndpointFunctionLowPrio {

  implicit def function0[Out] = new EndpointFunction[HNil, Out] {
    type Fun = Unit => Out
  }

  implicit def function1[KA, A, Out] = new EndpointFunction[FieldType[KA, A] :: HNil, Out] {
    type Fun = A => Out
  }

  implicit def function2[KA, A, KB, B, Out] = new EndpointFunction[FieldType[KA, A] :: FieldType[KB, B] :: HNil, Out] {
    type Fun = (A, B) => Out
  }

  implicit def function3[KA, A, KB, B, KC, C, Out] = new EndpointFunction[FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: HNil, Out] {
    type Fun = (A, B, C) => Out
  }

  implicit def function4[KA, A, KB, B, KC, C, KD, D, Out] = new EndpointFunction[FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: FieldType[KD, D] :: HNil, Out] {
    type Fun = (A, B, C, D) => Out
  }
}
