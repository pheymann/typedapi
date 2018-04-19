package typedapi.server

import shapeless._

import scala.language.higherKinds
import scala.annotation.implicitNotFound

@implicitNotFound("""Could not find FunctionApply instance. Support max arity is 8. If you need more implement this type-class.

input: ${VIn}
out: ${Out}""")
trait FunctionApply[VIn <: HList, Out] {

  type Fun[_[_]]

  def apply[F[_]](in: VIn, f: Fun[F]): F[Out]
}

object FunctionApply {

  type Aux[VIn <: HList, Fun0[_[_]], Out] = FunctionApply[VIn, Out] {
    type Fun[F[_]] = Fun0[F]
  }

}

trait FunctionApplyLowPrio {

  final class Function0[Out] extends FunctionApply[HNil, Out] {
    type Fun[F[_]] = () => F[Out]

    def apply[F[_]](in: HNil, f: Fun[F]): F[Out] = f()
  }

  implicit def funApply0[Out] = new Function0[Out]

  final class Function1[A, Out] extends FunctionApply[A :: HNil, Out] {
    type Fun[F[_]] = A => F[Out]

    def apply[F[_]](in: A :: HNil, f: Fun[F]): F[Out] = f(in.head)
  }

  implicit def funApply1[A, Out] = new Function1[A, Out]

  final class Function2[A, B, Out] extends FunctionApply[A :: B :: HNil, Out] {
    type Fun[F[_]] = (A, B) => F[Out]

    def apply[F[_]](in: A :: B :: HNil, f: Fun[F]): F[Out] = {
      val a :: b :: HNil = in

      f(a, b)
    }
  }

  implicit def funApply2[A, B, Out] = new Function2[A, B, Out]

  final class Function3[A, B, C, Out] extends FunctionApply[A :: B :: C :: HNil, Out] {
    type Fun[F[_]] = (A, B, C) => F[Out]

    def apply[F[_]](in: A :: B :: C :: HNil, f: Fun[F]): F[Out] = {
      val a :: b :: c :: HNil = in

      f(a, b, c)
    }
  }

  implicit def funApply3[A, B, C, Out] = new Function3[A, B, C, Out]

  final class Function4[A, B, C, D, Out] extends FunctionApply[A :: B :: C :: D :: HNil, Out] {
    type Fun[F[_]] = (A, B, C, D) => F[Out]

    def apply[F[_]](in: A :: B :: C :: D :: HNil, f: Fun[F]): F[Out] = {
      val a :: b :: c :: d :: HNil = in

      f(a, b, c, d)
    }
  }

  implicit def funApply4[A, B, C, D, Out] = new Function4[A, B, C, D, Out]

  final class Function5[A, B, C, D, E, Out] extends FunctionApply[A :: B :: C :: D :: E :: HNil, Out] {
    type Fun[F[_]] = (A, B, C, D, E) => F[Out]

    def apply[F[_]](in: A :: B :: C :: D :: E :: HNil, f: Fun[F]): F[Out] = {
      val a :: b :: c :: d :: e :: HNil = in

      f(a, b, c, d, e)
    }
  }

  implicit def funApply5[A, B, C, D, E, Out] = new Function5[A, B, C, D, E, Out]

  final class Function6[A, B, C, D, E, F, Out] extends FunctionApply[A :: B :: C :: D :: E :: F :: HNil, Out] {
    type Fun[M[_]] = (A, B, C, D, E, F) => M[Out]

    def apply[M[_]](in: A :: B :: C :: D :: E :: F :: HNil, _f: Fun[M]): M[Out] = {
      val a :: b :: c :: d :: e :: f :: HNil = in

      _f(a, b, c, d, e, f)
    }
  }

  implicit def funApply6[A, B, C, D, E, F, Out] = new Function6[A, B, C, D, E, F, Out]

  final class Function7[A, B, C, D, E, F, G, Out] extends FunctionApply[A :: B :: C :: D :: E :: F :: G :: HNil, Out] {
    type Fun[M[_]] = (A, B, C, D, E, F, G) => M[Out]

    def apply[M[_]](in: A :: B :: C :: D :: E :: F :: G :: HNil, f: Fun[M]): M[Out] = {
      val a :: b :: c :: d :: e :: _f :: g :: HNil = in

      f(a, b, c, d, e, _f, g)
    }
  }

  implicit def funApply7[A, B, C, D, E, F, G, Out] = new Function7[A, B, C, D, E, F, G, Out]

  final class Function8[A, B, C, D, E, F, G, H, Out] extends FunctionApply[A :: B :: C :: D :: E :: F :: G :: H :: HNil, Out] {
    type Fun[M[_]] = (A, B, C, D, E, F, G, H) => M[Out]

    def apply[M[_]](in: A :: B :: C :: D :: E :: F :: G :: H :: HNil, f: Fun[M]): M[Out] = {
      val a :: b :: c :: d :: e :: _f :: g :: h :: HNil = in

      f(a, b, c, d, e, _f, g, h)
    }
  }

  implicit def funApply8[A, B, C, D, E, F, G, H, Out] = new Function8[A, B, C, D, E, F, G, H, Out]
}
