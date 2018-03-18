package typedapi.server

import shapeless._
import shapeless.labelled.FieldType

import scala.language.higherKinds

sealed trait FunctionDef[In <: HList, Out] {

  type Fun[_[_]]
  type CIn <: HList

  def apply[F[_]](in: CIn, f: Fun[F]): F[Out]
}

object FunctionDef {

  type Aux[In <: HList, CIn0 <: HList, Fun0[_[_]], Out] = FunctionDef[In, Out] {
    type Fun[F[_]] = Fun0[F]
    type CIn       = CIn0
  }
}

trait FunctionDefLowPrio {

  final class Function0[Out] extends FunctionDef[HNil, Out] {
    type Fun[F[_]] = () => F[Out]
    type CIn       = HNil

    def apply[F[_]](in: HNil, f: Fun[F]): F[Out] = f()
  }

  implicit def funDef0[Out] = new Function0[Out]

  final class Function1[KA, A, Out] extends FunctionDef[FieldType[KA, A] :: HNil, Out] {
    type Fun[F[_]] = A => F[Out]
    type CIn = A :: HNil

    def apply[F[_]](in: A :: HNil, f: Fun[F]): F[Out] = f(in.head)
  }

  implicit def funDef1[KA, A, Out] = new Function1[KA, A, Out]

  final class Function2[KA, A, KB, B, Out] extends FunctionDef[FieldType[KA, A] :: FieldType[KB, B] :: HNil, Out] {
    type Fun[F[_]] = (A, B) => F[Out]
    type CIn = A :: B :: HNil

    def apply[F[_]](in: A :: B :: HNil, f: Fun[F]): F[Out] = {
      val a :: b :: HNil = in

      f(a, b)
    }
  }

  implicit def funDef2[KA, A, KB, B, Out] = new Function2[KA, A, KB, B, Out]

  final class Function3[KA, A, KB, B, KC, C, Out] extends FunctionDef[FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: HNil, Out] {
    type Fun[F[_]] = (A, B, C) => F[Out]
    type CIn = A :: B :: C :: HNil

    def apply[F[_]](in: A :: B :: C :: HNil, f: Fun[F]): F[Out] = {
      val a :: b :: c :: HNil = in

      f(a, b, c)
    }
  }

  implicit def funDef3[KA, A, KB, B, KC, C, Out] = new Function3[KA, A, KB, B, KC, C, Out]

  final class Function4[KA, A, KB, B, KC, C, KD, D, Out] extends FunctionDef[FieldType[KA, A] :: FieldType[KB, B] :: FieldType[KC, C] :: FieldType[KD, D] :: HNil, Out] {
    type Fun[F[_]] = (A, B, C, D) => F[Out]
    type CIn = A :: B :: C :: D :: HNil

    def apply[F[_]](in: A :: B :: C :: D :: HNil, f: Fun[F]): F[Out] = {
      val a :: b :: c :: d :: HNil = in

      f(a, b, c, d)
    }
  }

  implicit def funDef4[KA, A, KB, B, KC, C, KD, D, Out] = new Function4[KA, A, KB, B, KC, C, KD, D, Out]
}
