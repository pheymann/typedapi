package typedapi.server

import shapeless._

import scala.language.higherKinds
import scala.annotation.implicitNotFound

@implicitNotFound("""Could not find FunctionApply instance. Support max arity is 8. If you need more implement an instance for this type-class.

input: ${VIn}
out: ${Out}""")
trait FunctionApply[VIn <: HList, F[_], Out] {

  type Fn

  def apply(in: VIn, f: Fn): F[Out]
}

object FunctionApply {

  type Aux[VIn <: HList, Fn0, F[_], Out] = FunctionApply[VIn, F, Out] {
    type Fn = Fn0
  }

}

trait FunctionApplyLowPrio {

  implicit def funApply0[F[_], Out] = new FunctionApply[HNil, F, Out] {
    type Fn = () => F[Out]

    def apply(in: HNil, fn: Fn): F[Out] = fn()
  }

  implicit def funApply1[A, F[_], Out] = new FunctionApply[A :: HNil, F, Out] {
    type Fn = A => F[Out]

    def apply(in: A :: HNil, fn: Fn): F[Out] = fn(in.head)
  }

  implicit def funApply2[A, B, F[_], Out] = new FunctionApply[A :: B :: HNil, F, Out] {
    type Fn = (A, B) => F[Out]

    def apply(in: A :: B :: HNil, fn: Fn): F[Out] = {
      val a :: b :: HNil = in

      fn(a, b)
    }
  }

  implicit def funApply3[A, B, C, F[_], Out] = new FunctionApply[A :: B :: C :: HNil, F, Out] {
    type Fn = (A, B, C) => F[Out]

    def apply(in: A :: B :: C :: HNil, fn: Fn): F[Out] = {
      val a :: b :: c :: HNil = in

      fn(a, b, c)
    }
  }

  implicit def funApply4[A, B, C, D, F[_], Out] = new FunctionApply[A :: B :: C :: D :: HNil, F, Out] {
    type Fn = (A, B, C, D) => F[Out]

    def apply(in: A :: B :: C :: D :: HNil, fn: Fn): F[Out] = {
      val a :: b :: c :: d :: HNil = in

      fn(a, b, c, d)
    }
  }

  implicit def funApply5[A, B, C, D, E, F[_], Out] = new FunctionApply[A :: B :: C :: D :: E :: HNil, F, Out] {
    type Fn = (A, B, C, D, E) => F[Out]

    def apply(in: A :: B :: C :: D :: E :: HNil, fn: Fn): F[Out] = {
      val a :: b :: c :: d :: e :: HNil = in

      fn(a, b, c, d, e)
    }
  }

  implicit def funApply6[A, B, C, D, E, F, _F[_], Out] = new FunctionApply[A :: B :: C :: D :: E :: F :: HNil, _F, Out] {
    type Fn = (A, B, C, D, E, F) => _F[Out]

    def apply(in: A :: B :: C :: D :: E :: F :: HNil, fn: Fn): _F[Out] = {
      val a :: b :: c :: d :: e :: f :: HNil = in

      fn(a, b, c, d, e, f)
    }
  }

  implicit def funApply7[A, B, C, D, E, F, G, _F[_], Out] = new FunctionApply[A :: B :: C :: D :: E :: F :: G :: HNil, _F, Out] {
    type Fn = (A, B, C, D, E, F, G) => _F[Out]

    def apply(in: A :: B :: C :: D :: E :: F :: G :: HNil, fn: Fn): _F[Out] = {
      val a :: b :: c :: d :: e :: f :: g :: HNil = in

      fn(a, b, c, d, e, f, g)
    }
  }

  implicit def funApply8[A, B, C, D, E, F, G, H, _F[_], Out] = new FunctionApply[A :: B :: C :: D :: E :: F :: G :: H :: HNil, _F, Out] {
    type Fn = (A, B, C, D, E, F, G, H) => _F[Out]

    def apply(in: A :: B :: C :: D :: E :: F :: G :: H :: HNil, fn: Fn): _F[Out] = {
      val a :: b :: c :: d :: e :: _f :: g :: h :: HNil = in

      fn(a, b, c, d, e, _f, g, h)
    }
  }
}
