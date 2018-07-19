package typedapi.shared

import shapeless._

import scala.language.higherKinds

final case class TypeCarrier[A]()

final class PairTypeFromWitnessKey[F[_, _], V] {

  def apply[K](wit: Witness.Lt[K]): TypeCarrier[F[K, V]] = TypeCarrier()
}

final class PairTypeFromWitnesses[F[_, _]] {

  def apply[K, V](kWit: Witness.Lt[K], vWit: Witness.Lt[V]): TypeCarrier[F[K, V]] = TypeCarrier()
}

/** carriers the final api type, which is represented as `HList` */
final case class ApiTypeCarrier[H <: HList]() {

  def :|:[H1 <: HList](next: ApiTypeCarrier[H1]): CompositionCons[H1 :: H :: HNil] = CompositionCons()
}

/** carriers multiple api types represented as `HList`; therefore, it is a `HList` of `HList` */
final case class CompositionCons[H <: HList]() {

  def :|:[H1 <: HList](next: ApiTypeCarrier[H1]): CompositionCons[H1 :: H] = CompositionCons()
}
