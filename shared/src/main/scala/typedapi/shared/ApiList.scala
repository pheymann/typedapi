package typedapi.shared

import shapeless._

/** A final element is a method describing the request type. */
final case class ApiTypeCarrier[H <: HList]() {

  def :|:[H1 <: HList](next: ApiTypeCarrier[H1]): CompositionCons[H1 :: H :: HNil] = CompositionCons()
}

/** Compose multiple type level api descriptions in a HList of HLists.
  */
final case class CompositionCons[H <: HList]() {

  def :|:[H1 <: HList](next: ApiTypeCarrier[H1]): CompositionCons[H1 :: H] = CompositionCons()
}

sealed trait PathList[P <: HList]

final case class PathListCons[P <: HList]() extends PathList[P] {

  def :>[S](path: Witness.Lt[S]): PathListCons[PathElement[S] :: P] = PathListCons()
  def :>[S <: Symbol, A](segment: SegmentParam[S, A]): PathListCons[SegmentParam[S, A] :: P] = PathListCons()
}

case object PathListEmpty extends PathList[HNil] {

  def :>[S](path: Witness.Lt[S]): PathListCons[PathElement[S] :: HNil] = PathListCons()
  def :>[S <: Symbol, A](segment: SegmentParam[S, A]): PathListCons[SegmentParam[S, A] :: HNil] = PathListCons()
}

sealed trait QueryList[Q <: HList]

final case class QueryListCons[Q <: HList]() extends QueryList[Q] {

  def :>[S <: Symbol, A](query: QueryParam[S, A]): QueryListCons[QueryParam[S, A] :: Q] = QueryListCons()
}

case object QueryListEmpty extends QueryList[HNil] {

  def :>[S <: Symbol, A](query: QueryParam[S, A]): QueryListCons[QueryParam[S, A] :: HNil] = QueryListCons()
}

sealed trait HeaderList[H <: HList]

final case class HeaderListCons[H <: HList]() extends HeaderList[H] {

  def :>[S <: Symbol, A](header: HeaderParam[S, A]): HeaderListCons[HeaderParam[S, A] :: H] = HeaderListCons()
  def :>(headers: RawHeadersParam.type): RawHeaderCons[RawHeadersParam.type :: H] = RawHeaderCons()
}

final case class RawHeaderCons[H <: HList]() extends HeaderList[H]

case object HeaderListEmpty extends HeaderList[HNil] {

  def :>[S <: Symbol, A](header: HeaderParam[S, A]): HeaderListCons[HeaderParam[S, A] :: HNil] = HeaderListCons()
  def :>(headers: RawHeadersParam.type): RawHeaderCons[RawHeadersParam.type :: HNil] = RawHeaderCons()
}
