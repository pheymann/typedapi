package typedapi.server

import shapeless._

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

  implicit def function1[A, Out] = new EndpointFunction[A :: HNil, Out] {
    type Fun = A => Out
  }

  implicit def function2[A, B, Out] = new EndpointFunction[A :: B :: HNil, Out] {
    type Fun = (A, B) => Out
  }

  implicit def function3[A, B, C, Out] = new EndpointFunction[A :: B :: C :: HNil, Out] {
    type Fun = (A, B, C) => Out
  }

  implicit def function4[A, B, C, D, Out] = new EndpointFunction[A :: B :: C :: D :: HNil, Out] {
    type Fun = (A, B, C, D) => Out
  }
}
