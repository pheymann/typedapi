package typedapi.client

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

  def :>[A](headers: RawHeaders.type): RawHeadersCons[RawHeaders.type :: H] = RawHeadersCons()
  def :>[A](body: ReqBody[A]): WithBodyCons[A, H] = WithBodyCons()
  def :>[A](get: Get[A]): FinalCons[Get[A] :: H] = FinalCons()
  def :>[A](put: Put[A]): FinalCons[Put[A] :: H] = FinalCons()
  def :>[A](post: Post[A]): FinalCons[Post[A] :: H] = FinalCons()
  def :>[A](delete: Delete[A]): FinalCons[Delete[A] :: H] = FinalCons()
}

/** Initial element with empty api description. */
case object EmptyCons extends ApiListWithOps[HNil] {

  def :>[S](path: Witness.Lt[S]): PathCons[Path[S] :: HNil] = PathCons()
  def :>[S <: Symbol, A](segment: SegmentParam[S, A]): SegmentCons[SegmentParam[S, A] :: HNil] = SegmentCons()
  def :>[S <: Symbol, A](query: QueryParam[S, A]): QueryCons[QueryParam[S, A] :: HNil] = QueryCons()  
  def :>[S <: Symbol, A](header: HeaderParam[S, A]): HeaderCons[HeaderParam[S, A] :: HNil] = HeaderCons()
}

/** Last set element is a path. */
final case class PathCons[H <: HList]() extends ApiListWithOps[H] {

  def :>[S](path: Witness.Lt[S]): PathCons[Path[S] :: H] = PathCons()
  def :>[S <: Symbol, A](segment: SegmentParam[S, A]): SegmentCons[SegmentParam[S, A] :: H] = SegmentCons()
  def :>[S <: Symbol, A](query: QueryParam[S, A]): QueryCons[QueryParam[S, A] :: H] = QueryCons()  
  def :>[S <: Symbol, A](header: HeaderParam[S, A]): HeaderCons[HeaderParam[S, A] :: H] = HeaderCons()
}

/** Last set element is a segment. */
final case class SegmentCons[H <: HList]() extends ApiListWithOps[H] {

  def :>[S](path: Witness.Lt[S]): PathCons[Path[S] :: H] = PathCons()
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

  def :>[A](put: Put[A]): FinalCons[PutWithBody[Bd, A] :: H] = FinalCons()
  def :>[A](post: Post[A]): FinalCons[PostWithBody[Bd, A] :: H] = FinalCons()
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
