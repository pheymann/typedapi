package typedapi.server

import shapeless._
import shapeless.labelled.FieldType

sealed trait EndpointFunction[In <: HList, Out] {

  type Fun

  def apply(in: In, f: Fun): Out
}

object EndpointFunction {

  type Aux[In <: HList, Out, Fun0] = EndpointFunction[In, Out] { type Fun = Fun0 }
}

trait EndpointFunctionLowPrio {

  implicit def function0[Out] = new EndpointFunction[HNil, Out] {
    type Fun = () => Out

    def apply(in: HNil, f: Fun): Out = f()
  }

  implicit def function1[KA, A, Out] = new EndpointFunction[FieldType[KA, A] :: HNil, Out] {
    type Fun = A => Out

    def apply(in: FieldType[KA, A] :: HNil, f: Fun): Out = f(in.head)
  }

  implicit def function2[KA, A, KB, B, Out] = new EndpointFunction[FieldType[KA, A] :: FieldType[KB, B] :: HNil, Out] {
    type Fun = (A, B) => Out

    def apply(in: FieldType[KA, A] :: FieldType[KB, B] :: HNil, f: Fun): Out = {
      val a :: b :: HNil = in

      f(a, b)
    }
  }

  implicit def function3[KA, A, KB, B, KC, C, Out] = new EndpointFunction[FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: HNil, Out] {
    type Fun = (A, B, C) => Out

    def apply(in: FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: HNil, f: Fun): Out = {
      val a :: b :: c :: HNil = in

      f(a, b, c)
    }
  }

  implicit def function4[KA, A, KB, B, KC, C, KD, D, Out] = new EndpointFunction[FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: FieldType[KD, D] :: HNil, Out] {
    type Fun = (A, B, C, D) => Out

    def apply(in: FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: FieldType[KD, D] :: HNil, f: Fun): Out = {
      val a :: b :: c :: d :: HNil = in

      f(a, b, c, d)
    }
  }
}
