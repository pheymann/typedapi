package typedapi.server

import shapeless._
import shapeless.labelled.FieldType

import scala.language.higherKinds
import scala.annotation.implicitNotFound

@implicitNotFound("""Could not find FunctionApply instance. Support max arity is 8. If you need more implement this type-class.

input: ${In}""")
trait FunctionApply[In <: HList, Out] {

  type Fun[_[_]]
  type CIn <: HList

  def apply[F[_]](in: CIn, f: Fun[F]): F[Out]
}

object FunctionApply {

  type Aux[In <: HList, CIn0 <: HList, Fun0[_[_]], Out] = FunctionApply[In, Out] {
    type Fun[F[_]] = Fun0[F]
    type CIn       = CIn0
  }
}

trait FunctionApplyLowPrio {

  final class Function0[Out] extends FunctionApply[HNil, Out] {
    type Fun[F[_]] = () => F[Out]
    type CIn       = HNil

    def apply[F[_]](in: HNil, f: Fun[F]): F[Out] = f()
  }

  implicit def funApply0[Out] = new Function0[Out]

  final class Function1[KA, A, Out] extends FunctionApply[FieldType[KA, A] :: HNil, Out] {
    type Fun[F[_]] = A => F[Out]
    type CIn = A :: HNil

    def apply[F[_]](in: A :: HNil, f: Fun[F]): F[Out] = f(in.head)
  }

  implicit def funApply1[KA, A, Out] = new Function1[KA, A, Out]

  final class Function2[KA, A, KB, B, Out] extends FunctionApply[FieldType[KA, A] :: FieldType[KB, B] :: HNil, Out] {
    type Fun[F[_]] = (A, B) => F[Out]
    type CIn = A :: B :: HNil

    def apply[F[_]](in: A :: B :: HNil, f: Fun[F]): F[Out] = {
      val a :: b :: HNil = in

      f(a, b)
    }
  }

  implicit def funApply2[KA, A, KB, B, Out] = new Function2[KA, A, KB, B, Out]

  final class Function3[KA, A, KB, B, KC, C, Out] extends FunctionApply[FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: HNil, Out] {
    type Fun[F[_]] = (A, B, C) => F[Out]
    type CIn = A :: B :: C :: HNil

    def apply[F[_]](in: A :: B :: C :: HNil, f: Fun[F]): F[Out] = {
      val a :: b :: c :: HNil = in

      f(a, b, c)
    }
  }

  implicit def funApply3[KA, A, KB, B, KC, C, Out] = new Function3[KA, A, KB, B, KC, C, Out]

  final class Function4[KA, A, KB, B, KC, C, KD, D, Out] extends FunctionApply[FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: FieldType[KD, D] :: HNil, Out] {
    type Fun[F[_]] = (A, B, C, D) => F[Out]
    type CIn = A :: B :: C :: D :: HNil

    def apply[F[_]](in: A :: B :: C :: D :: HNil, f: Fun[F]): F[Out] = {
      val a :: b :: c :: d :: HNil = in

      f(a, b, c, d)
    }
  }

  implicit def funApply4[KA, A, KB, B, KC, C, KD, D, Out] = new Function4[KA, A, KB, B, KC, C, KD, D, Out]

  final class Function5[KA, A, KB, B, KC, C, KD, D, KE, E, Out] extends FunctionApply[FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: FieldType[KD, D] :: FieldType[KE, E] :: HNil, Out] {
    type Fun[F[_]] = (A, B, C, D, E) => F[Out]
    type CIn = A :: B :: C :: D :: E :: HNil

    def apply[F[_]](in: A :: B :: C :: D :: E :: HNil, f: Fun[F]): F[Out] = {
      val a :: b :: c :: d :: e :: HNil = in

      f(a, b, c, d, e)
    }
  }

  implicit def funApply5[KA, A, KB, B, KC, C, KD, D, KE, E, Out] = new Function5[KA, A, KB, B, KC, C, KD, D, KE, E, Out]

  final class Function6[KA, A, KB, B, KC, C, KD, D, KE, E, KF, F, Out] extends FunctionApply[FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: FieldType[KD, D] :: FieldType[KA, A] :: FieldType[KF, F] :: HNil, Out] {
    type Fun[M[_]] = (A, B, C, D, E, F) => M[Out]
    type CIn = A :: B :: C :: D :: E :: F :: HNil

    def apply[M[_]](in: A :: B :: C :: D :: E :: F :: HNil, _f: Fun[M]): M[Out] = {
      val a :: b :: c :: d :: e :: f :: HNil = in

      _f(a, b, c, d, e, f)
    }
  }

  implicit def funApply6[KA, A, KB, B, KC, C, KD, D, KE, E, KF, F, Out] = new Function6[KA, A, KB, B, KC, C, KD, D, KE, E, KF, F, Out]

  final class Function7[KA, A, KB, B, KC, C, KD, D, KE, E, KF, F, KG, G, Out] extends FunctionApply[FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: FieldType[KD, D] :: FieldType[KA, A] :: FieldType[KF, F] :: FieldType[KG, G] :: HNil, Out] {
    type Fun[M[_]] = (A, B, C, D, E, F, G) => M[Out]
    type CIn = A :: B :: C :: D :: E :: F :: G :: HNil

    def apply[M[_]](in: A :: B :: C :: D :: E :: F :: G :: HNil, f: Fun[M]): M[Out] = {
      val a :: b :: c :: d :: e :: _f :: g :: HNil = in

      f(a, b, c, d, e, _f, g)
    }
  }

  implicit def funApply7[KA, A, KB, B, KC, C, KD, D, KE, E, KF, F, KG, G, Out] = new Function7[KA, A, KB, B, KC, C, KD, D, KE, E, KF, F, KG, G, Out]

  final class Function8[KA, A, KB, B, KC, C, KD, D, KE, E, KF, F, KG, G, KH, H, Out] extends FunctionApply[FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: FieldType[KD, D] :: FieldType[KA, A] :: FieldType[KF, F] :: FieldType[KG, G] :: FieldType[KH, H] :: HNil, Out] {
    type Fun[M[_]] = (A, B, C, D, E, F, G, H) => M[Out]
    type CIn = A :: B :: C :: D :: E :: F :: G :: H :: HNil

    def apply[M[_]](in: A :: B :: C :: D :: E :: F :: G :: H :: HNil, f: Fun[M]): M[Out] = {
      val a :: b :: c :: d :: e :: _f :: g :: h :: HNil = in

      f(a, b, c, d, e, _f, g, h)
    }
  }

  implicit def funApply8[KA, A, KB, B, KC, C, KD, D, KE, E, KF, F, KG, G, KH, H, Out] = new Function8[KA, A, KB, B, KC, C, KD, D, KE, E, KF, F, KG, G, KH, H, Out]
}
