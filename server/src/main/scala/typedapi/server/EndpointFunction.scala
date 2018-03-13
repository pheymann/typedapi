package typedapi.server

import shapeless._
import shapeless.labelled.FieldType

sealed trait EndpointFunction[In <: HList, Out] {

  type Fun
  type CIn <: HList

  def apply(in: CIn, f: Fun): Out
}

object EndpointFunction {

  type Aux[In <: HList, CIn0 <: HList, Fun0, Out] = EndpointFunction[In, Out] { 
    type Fun = Fun0 
    type CIn = CIn0
  }
}

trait EndpointFunctionLowPrio {

  implicit def function0[Out] = new EndpointFunction[HNil, Out] {
    type Fun = () => Out
    type CIn = HNil

    def apply(in: CIn, f: Fun): Out = f()
  }

  implicit def function1[KA, A, Out] = new EndpointFunction[FieldType[KA, A] :: HNil, Out] {
    type Fun = A => Out
    type CIn = A :: HNil

    def apply(in: CIn, f: Fun): Out = f(in.head)
  }

  implicit def function2[KA, A, KB, B, Out] = new EndpointFunction[FieldType[KA, A] :: FieldType[KB, B] :: HNil, Out] {
    type Fun = (A, B) => Out
    type CIn = A :: B :: HNil

    def apply(in: CIn, f: Fun): Out = {
      val a :: b :: HNil = in

      f(a, b)
    }
  }

  implicit def function3[KA, A, KB, B, KC, C, Out] = new EndpointFunction[FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: HNil, Out] {
    type Fun = (A, B, C) => Out
    type CIn = A :: B :: C :: HNil

    def apply(in: CIn, f: Fun): Out = {
      val a :: b :: c :: HNil = in

      f(a, b, c)
    }
  }

  implicit def function4[KA, A, KB, B, KC, C, KD, D, Out] = new EndpointFunction[FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: FieldType[KD, D] :: HNil, Out] {
    type Fun = (A, B, C, D) => Out
    type CIn = A :: B :: C :: D :: HNil

    def apply(in: CIn, f: Fun): Out = {
      val a :: b :: c :: d :: HNil = in

      f(a, b, c, d)
    }
  }
}
