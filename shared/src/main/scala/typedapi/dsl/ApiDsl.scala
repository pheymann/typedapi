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

  def :>[MT <: MediaType, A](body: ReqBodyElement[MT, A]): WithBodyCons[MT, A, H] = WithBodyCons()
  def :>[MT <: MediaType, A](get: GetElement[MT, A]): ApiTypeCarrier[GetElement[MT, A] :: H] = ApiTypeCarrier()
  def :>[MT <: MediaType, A](put: PutElement[MT, A]): ApiTypeCarrier[PutElement[MT, A] :: H] = ApiTypeCarrier()
  def :>[MT <: MediaType, A](post: PostElement[MT, A]): ApiTypeCarrier[PostElement[MT, A] :: H] = ApiTypeCarrier()
  def :>[MT <: MediaType, A](delete: DeleteElement[MT, A]): ApiTypeCarrier[DeleteElement[MT, A] :: H] = ApiTypeCarrier()
}

/** Initial element with empty api description. */
case object EmptyCons extends ApiListWithOps[HNil] {

  def :>[S](path: Witness.Lt[S]): PathCons[PathElement[S] :: HNil] = PathCons()
  def :>[K, V](segment: TypeCarrier[SegmentParam[K, V]]): SegmentCons[SegmentParam[K, V] :: HNil] = SegmentCons()
  def :>[K, V](query: TypeCarrier[QueryParam[K, V]]): QueryCons[QueryParam[K, V] :: HNil] = QueryCons()  
  def :>[K, V](header: TypeCarrier[HeaderParam[K, V]]): HeaderCons[HeaderParam[K, V] :: HNil] = HeaderCons()
}

/** Last set element is a path. */
final case class PathCons[H <: HList]() extends ApiListWithOps[H] {

  def :>[S](path: Witness.Lt[S]): PathCons[PathElement[S] :: H] = PathCons()
  def :>[K, V](segment: TypeCarrier[SegmentParam[K, V]]): SegmentCons[SegmentParam[K, V] :: H] = SegmentCons()
  def :>[K, V](query: TypeCarrier[QueryParam[K, V]]): QueryCons[QueryParam[K, V] :: H] = QueryCons()  
  def :>[K, V](header: TypeCarrier[HeaderParam[K, V]]): HeaderCons[HeaderParam[K, V] :: H] = HeaderCons()
}

/** Last set element is a segment. */
final case class SegmentCons[H <: HList]() extends ApiListWithOps[H] {

  def :>[S](path: Witness.Lt[S]): PathCons[PathElement[S] :: H] = PathCons()
  def :>[K, V](segment: TypeCarrier[SegmentParam[K, V]]): SegmentCons[SegmentParam[K, V] :: H] = SegmentCons()
  def :>[K, V](query: TypeCarrier[QueryParam[K, V]]): QueryCons[QueryParam[K, V] :: H] = QueryCons()
  def :>[K, V](header: TypeCarrier[HeaderParam[K, V]]): HeaderCons[HeaderParam[K, V] :: H] = HeaderCons()
}

/** Last set element is a query parameter. */
final case class QueryCons[H <: HList]() extends ApiListWithOps[H]  {

  def :>[K, V](query: TypeCarrier[QueryParam[K, V]]): QueryCons[QueryParam[K, V] :: H] = QueryCons()
  def :>[K, V](header: TypeCarrier[HeaderParam[K, V]]): HeaderCons[HeaderParam[K, V] :: H] = HeaderCons()
}

/** Last set element is a header. */
final case class HeaderCons[H <: HList]() extends ApiListWithOps[H] {

  def :>[K, V](header: TypeCarrier[HeaderParam[K, V]]): HeaderCons[HeaderParam[K, V] :: H] = HeaderCons()
}

/** Last set element is a request body. */
final case class WithBodyCons[BMT <: MediaType, Bd, H <: HList]() extends ApiList[H] {

  def :>[MT <: MediaType, A](put: PutElement[MT, A]): ApiTypeCarrier[PutWithBodyElement[BMT, Bd, MT, A] :: H] = ApiTypeCarrier()
  def :>[MT <: MediaType, A](post: PostElement[MT, A]): ApiTypeCarrier[PostWithBodyElement[BMT, Bd, MT, A] :: H] = ApiTypeCarrier()
}
