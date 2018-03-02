package typedapi.shared

import shapeless._

/** Type level api representation encoded as HList. This wrapper is used to encapsulate shapeless code
  * and enforces api structure:
  *   path    -> all
  *   segment -> all
  *   query   -> [query, header, body, method]
  *   header  -> [header, body, method]
  *   body    -> [method]
  *   method  -> nothing
  */
sealed trait ApiList[H <: HList]

/** Basic operations. */
sealed trait ApiListWithOps[H <: HList] extends ApiList[H] {

  def :>[A](headers: RawHeadersParam.type): RawHeadersCons[RawHeadersParam.type :: H] = RawHeadersCons()
  def :>[A](body: ReqBodyElement[A]): WithBodyCons[A, H] = WithBodyCons()
  def :>[A](get: GetElement[A]): FinalCons[GetElement[A] :: H] = FinalCons()
  def :>[A](put: PutElement[A]): FinalCons[PutElement[A] :: H] = FinalCons()
  def :>[A](post: PostElement[A]): FinalCons[PostElement[A] :: H] = FinalCons()
  def :>[A](delete: DeleteElement[A]): FinalCons[DeleteElement[A] :: H] = FinalCons()
}

/** Initial element with empty api description. */
case object EmptyCons extends ApiListWithOps[HNil] {

  def :>[S](path: Witness.Lt[S]): PathCons[PathElement[S] :: HNil] = PathCons()
  def :>[S <: Symbol, A](segment: SegmentParam[S, A]): SegmentCons[SegmentParam[S, A] :: HNil] = SegmentCons()
  def :>[S <: Symbol, A](query: QueryParam[S, A]): QueryCons[QueryParam[S, A] :: HNil] = QueryCons()  
  def :>[S <: Symbol, A](header: HeaderParam[S, A]): HeaderCons[HeaderParam[S, A] :: HNil] = HeaderCons()
}

/** Last set element is a path. */
final case class PathCons[H <: HList]() extends ApiListWithOps[H] {

  def :>[S](path: Witness.Lt[S]): PathCons[PathElement[S] :: H] = PathCons()
  def :>[S <: Symbol, A](segment: SegmentParam[S, A]): SegmentCons[SegmentParam[S, A] :: H] = SegmentCons()
  def :>[S <: Symbol, A](query: QueryParam[S, A]): QueryCons[QueryParam[S, A] :: H] = QueryCons()  
  def :>[S <: Symbol, A](header: HeaderParam[S, A]): HeaderCons[HeaderParam[S, A] :: H] = HeaderCons()
}

/** Last set element is a segment. */
final case class SegmentCons[H <: HList]() extends ApiListWithOps[H] {

  def :>[S](path: Witness.Lt[S]): PathCons[PathElement[S] :: H] = PathCons()
  def :>[S <: Symbol, A](segment: SegmentParam[S, A]): SegmentCons[SegmentParam[S, A] :: H] = SegmentCons()
  def :>[S <: Symbol, A](query: QueryParam[S, A]): QueryCons[QueryParam[S, A] :: H] = QueryCons()
  def :>[S <: Symbol, A](header: HeaderParam[S, A]): HeaderCons[HeaderParam[S, A] :: H] = HeaderCons()
}

/** Last set element is a query parameter. */
final case class QueryCons[H <: HList]() extends ApiListWithOps[H]  {

  def :>[S <: Symbol, A](query: QueryParam[S, A]): QueryCons[QueryParam[S, A] :: H] = QueryCons()
  def :>[S <: Symbol, A](header: HeaderParam[S, A]): HeaderCons[HeaderParam[S, A] :: H] = HeaderCons()
}

/** Last set element is a header. */
final case class HeaderCons[H <: HList]() extends ApiListWithOps[H] {

  def :>[S <: Symbol, A](header: HeaderParam[S, A]): HeaderCons[HeaderParam[S, A] :: H] = HeaderCons()
}

/** Last set element is a header. */
final case class RawHeadersCons[H <: HList]() extends ApiListWithOps[H]


/** Last set element is a request body. */
final case class WithBodyCons[Bd, H <: HList]() extends ApiList[H] {

  def :>[A](put: PutElement[A]): FinalCons[PutWithBodyElement[Bd, A] :: H] = FinalCons()
  def :>[A](post: PostElement[A]): FinalCons[PostWithBodyElement[Bd, A] :: H] = FinalCons()
}

/** A final element is a method describing the request type. */
final case class FinalCons[H <: HList]() extends ApiList[H] {

  def :|:[H1 <: HList](next: FinalCons[H1]): CompositionCons[H1 :: H :: HNil] = CompositionCons()
}

/** Compose multiple type level api descriptions in a HList of HLists.
  */
final case class CompositionCons[H <: HList]() extends ApiList[H] {

  def :|:[H1 <: HList](next: FinalCons[H1]): CompositionCons[H1 :: H] = CompositionCons()
}
