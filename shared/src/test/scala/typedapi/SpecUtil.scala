package typedapi

import shapeless.HList

import scala.language.higherKinds

object SpecUtil {

  class TestHelper[Act <: HList] {

    def apply[Exp <: HList](implicit ev: Act =:= Exp) = Unit
  }

  def testCompile[F[_ <: HList], Act <: HList](cons: F[Act]) = new TestHelper[Act]
}
