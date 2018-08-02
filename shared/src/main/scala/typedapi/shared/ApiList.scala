package typedapi.shared

import shapeless._

/** Typecarrier to construct a complete path description from [[PathElement]]s and [[SegmentParam]]s. */
final case class PathListBuilder[P <: HList]() {

  def /[S](path: Witness.Lt[S]): PathListBuilder[PathElement[S] :: P] = PathListBuilder()
  def /[K, V](segment: TypeCarrier[SegmentParam[K, V]]): PathListBuilder[SegmentParam[K, V] :: P] = PathListBuilder()
}

/** Typecarrier to construct a set of queries from [[QueryParam]]s. */
final case class QueryListBuilder[Q <: HList]() {

  final class WitnessDerivation[V] {
    def apply[K](wit: Witness.Lt[K]): QueryListBuilder[QueryParam[K, V] :: Q] = QueryListBuilder()
  }

  def add[V]: WitnessDerivation[V] = new WitnessDerivation[V]
}

/** Typecarrier to construct a set of headers from [[HeaderParam]]s, [[FixedHeaderElement]]s, [[ClientHeaderElement]]s and [[ServerHeaderElement]]s. */
final case class HeaderListBuilder[H <: HList]() {

  final class WitnessDerivation[V] {
    def apply[K](wit: Witness.Lt[K]): HeaderListBuilder[HeaderParam[K, V] :: H] = HeaderListBuilder()
  }

  def add[V]: WitnessDerivation[V] = new WitnessDerivation[V]
  def add[K, V](kWit: Witness.Lt[K], vWit: Witness.Lt[V]): HeaderListBuilder[FixedHeaderElement[K, V] :: H] = HeaderListBuilder()
  def client[K, V](kWit: Witness.Lt[K], vWit: Witness.Lt[V]): HeaderListBuilder[ClientHeaderElement[K, V] :: H] = HeaderListBuilder()
  def server[K, V](kWit: Witness.Lt[K], vWit: Witness.Lt[V]): HeaderListBuilder[ServerHeaderElement[K, V] :: H] = HeaderListBuilder()
}
