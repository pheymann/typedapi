package typedapi.shared

import shapeless._

import scala.language.higherKinds

/** As the name says this case class is only there it pass types around on the value level. */
final case class TypeCarrier[A]()

/** Derive a [[TypeCarrier]] from a type parameter and a singleton type. */
final class PairTypeFromWitnessKey[F[_, _], V] {

  def apply[K](wit: Witness.Lt[K]): TypeCarrier[F[K, V]] = TypeCarrier()
}

/** Derive a [[TypeCarrier]] from two singleton types. */
final class PairTypeFromWitnesses[F[_, _]] {

  def apply[K, V](kWit: Witness.Lt[K], vWit: Witness.Lt[V]): TypeCarrier[F[K, V]] = TypeCarrier()
}

/** Specific [[TypeCarrier]] for complete API types. */
final case class ApiTypeCarrier[H <: HList]() {

  def :|:[H1 <: HList](next: ApiTypeCarrier[H1]): CompositionCons[H1 :: H :: HNil] = CompositionCons()
}

/** Specific [[TypeCarrier]] for multiple API types. */
final case class CompositionCons[H <: HList]() {

  def :|:[H1 <: HList](next: ApiTypeCarrier[H1]): CompositionCons[H1 :: H] = CompositionCons()
}
