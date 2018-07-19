package typedapi.shared

import shapeless._

sealed trait PathList[P <: HList]

final case class PathListCons[P <: HList]() extends PathList[P] {

  def /[S](path: Witness.Lt[S]): PathListCons[PathElement[S] :: P] = PathListCons()
  def /[K, V](segment: TypeCarrier[SegmentParam[K, V]]): PathListCons[SegmentParam[K, V] :: P] = PathListCons()
}

case object PathListEmpty extends PathList[HNil] {

  def /[S](path: Witness.Lt[S]): PathListCons[PathElement[S] :: HNil] = PathListCons()
  def /[K, V](segment: TypeCarrier[SegmentParam[K, V]]): PathListCons[SegmentParam[K, V] :: HNil] = PathListCons()
}

final case class QueryListBuilder[Q <: HList]() {

  final class WitnessDerivation[V] {
    def apply[K](wit: Witness.Lt[K]): QueryListBuilder[QueryParam[K, V] :: Q] = QueryListBuilder()
  }

  def add[V]: WitnessDerivation[V] = new WitnessDerivation[V]
}

final case class HeaderListBuilder[H <: HList]() {

  final class WitnessDerivation[V] {
    def apply[K](wit: Witness.Lt[K]): HeaderListBuilder[HeaderParam[K, V] :: H] = HeaderListBuilder()
  }

  def add[V]: WitnessDerivation[V] = new WitnessDerivation[V]
  def add[K, V](kWit: Witness.Lt[K], vWit: Witness.Lt[V]): HeaderListBuilder[FixedHeaderElement[K, V] :: H] = HeaderListBuilder()
  def client[K, V](kWit: Witness.Lt[K], vWit: Witness.Lt[V]): HeaderListBuilder[ClientHeaderElement[K, V] :: H] = HeaderListBuilder()
  def server[K, V](kWit: Witness.Lt[K], vWit: Witness.Lt[V]): HeaderListBuilder[ServerHeaderElement[K, V] :: H] = HeaderListBuilder()
}
