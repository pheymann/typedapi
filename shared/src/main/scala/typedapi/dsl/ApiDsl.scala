package typedapi.dsl

import typedapi.shared._
import shapeless._

sealed trait ApiList[H <: HList]

/** Basic operations. */
sealed trait ApiListWithOps[H <: HList] extends ApiList[H] {

  def :>[MT <: MediaType, A](body: TypeCarrier[ReqBodyElement[MT, A]]): WithBodyCons[MT, A, H] = WithBodyCons()
  def :>[M <: MethodElement](method: TypeCarrier[M]): ApiTypeCarrier[M :: H] = ApiTypeCarrier()
}

/** Initial element with empty api description. */
case object EmptyCons extends ApiListWithOps[HNil] {

  def :>[S](path: Witness.Lt[S]): PathCons[PathElement[S] :: HNil] = PathCons()
  def :>[K, V](segment: TypeCarrier[SegmentParam[K, V]]): SegmentCons[SegmentParam[K, V] :: HNil] = SegmentCons()
  def :>[K, V](query: TypeCarrier[QueryParam[K, V]]): QueryCons[QueryParam[K, V] :: HNil] = QueryCons()  
  def :>[K, V](header: TypeCarrier[HeaderParam[K, V]]): InputHeaderCons[HeaderParam[K, V] :: HNil] = InputHeaderCons()
  def :>[K, V](fixed: TypeCarrier[FixedHeaderElement[K, V]]): FixedHeaderCons[FixedHeaderElement[K, V] :: HNil] = FixedHeaderCons()
  def :>[K, V](client: TypeCarrier[ClientHeaderElement[K, V]]): ClientHeaderCons[ClientHeaderElement[K, V] :: HNil] = ClientHeaderCons()
  def :>[K, V](server: TypeCarrier[ServerHeaderElement[K, V]]): ServerHeaderCons[ServerHeaderElement[K, V] :: HNil] = ServerHeaderCons()
}

/** Last set element is a path. */
final case class PathCons[H <: HList]() extends ApiListWithOps[H] {

  def :>[S](path: Witness.Lt[S]): PathCons[PathElement[S] :: H] = PathCons()
  def :>[K, V](segment: TypeCarrier[SegmentParam[K, V]]): SegmentCons[SegmentParam[K, V] :: H] = SegmentCons()
  def :>[K, V](query: TypeCarrier[QueryParam[K, V]]): QueryCons[QueryParam[K, V] :: H] = QueryCons()  
  def :>[K, V](header: TypeCarrier[HeaderParam[K, V]]): InputHeaderCons[HeaderParam[K, V] :: H] = InputHeaderCons()
  def :>[K, V](fixed: TypeCarrier[FixedHeaderElement[K, V]]): FixedHeaderCons[FixedHeaderElement[K, V] :: H] = FixedHeaderCons()
  def :>[K, V](client: TypeCarrier[ClientHeaderElement[K, V]]): ClientHeaderCons[ClientHeaderElement[K, V] :: H] = ClientHeaderCons()
  def :>[K, V](server: TypeCarrier[ServerHeaderElement[K, V]]): ServerHeaderCons[ServerHeaderElement[K, V] :: H] = ServerHeaderCons()
}

/** Last set element is a segment. */
final case class SegmentCons[H <: HList]() extends ApiListWithOps[H] {

  def :>[S](path: Witness.Lt[S]): PathCons[PathElement[S] :: H] = PathCons()
  def :>[K, V](segment: TypeCarrier[SegmentParam[K, V]]): SegmentCons[SegmentParam[K, V] :: H] = SegmentCons()
  def :>[K, V](query: TypeCarrier[QueryParam[K, V]]): QueryCons[QueryParam[K, V] :: H] = QueryCons()
  def :>[K, V](header: TypeCarrier[HeaderParam[K, V]]): InputHeaderCons[HeaderParam[K, V] :: H] = InputHeaderCons()
  def :>[K, V](fixed: TypeCarrier[FixedHeaderElement[K, V]]): FixedHeaderCons[FixedHeaderElement[K, V] :: H] = FixedHeaderCons()
  def :>[K, V](client: TypeCarrier[ClientHeaderElement[K, V]]): ClientHeaderCons[ClientHeaderElement[K, V] :: H] = ClientHeaderCons()
  def :>[K, V](server: TypeCarrier[ServerHeaderElement[K, V]]): ServerHeaderCons[ServerHeaderElement[K, V] :: H] = ServerHeaderCons()
}

/** Last set element is a query parameter. */
final case class QueryCons[H <: HList]() extends ApiListWithOps[H]  {

  def :>[K, V](query: TypeCarrier[QueryParam[K, V]]): QueryCons[QueryParam[K, V] :: H] = QueryCons()
  def :>[K, V](header: TypeCarrier[HeaderParam[K, V]]): InputHeaderCons[HeaderParam[K, V] :: H] = InputHeaderCons()
  def :>[K, V](fixed: TypeCarrier[FixedHeaderElement[K, V]]): FixedHeaderCons[FixedHeaderElement[K, V] :: H] = FixedHeaderCons()
  def :>[K, V](client: TypeCarrier[ClientHeaderElement[K, V]]): ClientHeaderCons[ClientHeaderElement[K, V] :: H] = ClientHeaderCons()
  def :>[K, V](server: TypeCarrier[ServerHeaderElement[K, V]]): ServerHeaderCons[ServerHeaderElement[K, V] :: H] = ServerHeaderCons()
}

/** Last set element is a header. */
sealed trait HeaderCons[H <: HList] extends ApiListWithOps[H] {

  def :>[K, V](header: TypeCarrier[HeaderParam[K, V]]): InputHeaderCons[HeaderParam[K, V] :: H] = InputHeaderCons()
  def :>[K, V](fixed: TypeCarrier[FixedHeaderElement[K, V]]): FixedHeaderCons[FixedHeaderElement[K, V] :: H] = FixedHeaderCons()
  def :>[K, V](client: TypeCarrier[ClientHeaderElement[K, V]]): ClientHeaderCons[ClientHeaderElement[K, V] :: H] = ClientHeaderCons()
  def :>[K, V](server: TypeCarrier[ServerHeaderElement[K, V]]): ServerHeaderCons[ServerHeaderElement[K, V] :: H] = ServerHeaderCons()
}

final case class InputHeaderCons[H <: HList]() extends HeaderCons[H]
final case class FixedHeaderCons[H <: HList]() extends HeaderCons[H]
final case class ClientHeaderCons[H <: HList]() extends HeaderCons[H]
final case class ServerHeaderCons[H <: HList]() extends HeaderCons[H]

/** Last set element is a request body. */
final case class WithBodyCons[BMT <: MediaType, Bd, H <: HList]() extends ApiList[H] {

  def :>[M <: MethodElement](method: TypeCarrier[M])(implicit out: MethodToReqBody[M, BMT, Bd]): ApiTypeCarrier[out.Out :: H] = ApiTypeCarrier()
}
