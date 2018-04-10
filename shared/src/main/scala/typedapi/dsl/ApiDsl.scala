package typedapi.dsl

import typedapi.shared._
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

  def :>(headers: RawHeadersParam.type): RawHeadersCons[RawHeadersParam.type :: H] = RawHeadersCons()
  def :>[A](body: ReqBodyElement[A]): WithBodyCons[A, H] = WithBodyCons()
  def :>[A](get: GetElement[A]): ApiTypeCarrier[GetElement[A] :: H] = ApiTypeCarrier()
  def :>[A](put: PutElement[A]): ApiTypeCarrier[PutElement[A] :: H] = ApiTypeCarrier()
  def :>[A](post: PostElement[A]): ApiTypeCarrier[PostElement[A] :: H] = ApiTypeCarrier()
  def :>[A](delete: DeleteElement[A]): ApiTypeCarrier[DeleteElement[A] :: H] = ApiTypeCarrier()
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

  def :>[A](put: PutElement[A]): ApiTypeCarrier[PutWithBodyElement[Bd, A] :: H] = ApiTypeCarrier()
  def :>[A](post: PostElement[A]): ApiTypeCarrier[PostWithBodyElement[Bd, A] :: H] = ApiTypeCarrier()
}
