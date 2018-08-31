package typedapi.dsl

import typedapi.shared._
import shapeless._

sealed trait ApiList[H <: HList]

/** Basic operations. */
sealed trait MethodOps[H <: HList] {

  def :>[MT <: MediaType, A](body: TypeCarrier[ReqBodyElement[MT, A]]): WithBodyCons[MT, A, H] = WithBodyCons()
  def :>[M <: MethodElement](method: TypeCarrier[M]): ApiTypeCarrier[M :: H] = ApiTypeCarrier()
}

sealed trait PathOps[H <: HList] {

  def :>[S](path: Witness.Lt[S]): PathCons[PathElement[S] :: H] = PathCons()
  def :>[K, V](segment: TypeCarrier[SegmentParam[K, V]]): SegmentCons[SegmentParam[K, V] :: H] = SegmentCons()
}

sealed trait HeaderOps[H <: HList] {

  def :>[K, V](header: TypeCarrier[HeaderParam[K, V]]): InputHeaderCons[HeaderParam[K, V] :: H] = InputHeaderCons()
  def :>[K, V](fixed: TypeCarrier[FixedHeaderElement[K, V]]): FixedHeaderCons[FixedHeaderElement[K, V] :: H] = FixedHeaderCons()

  def :>[K, V](client: TypeCarrier[ClientHeaderElement[K, V]]): ClientHeaderElCons[ClientHeaderElement[K, V] :: H] = ClientHeaderElCons()
  def :>[K, V](client: TypeCarrier[ClientHeaderParam[K, V]]): ClientHeaderParamCons[ClientHeaderParam[K, V] :: H] = ClientHeaderParamCons()
  def :>[V](client: TypeCarrier[ClientHeaderCollParam[V]]): ClientHeaderCollParamCons[ClientHeaderCollParam[V] :: H] = ClientHeaderCollParamCons()

  def :>[K, V](server: TypeCarrier[ServerHeaderMatchParam[K, V]]): ServerHeaderMatchParamCons[ServerHeaderMatchParam[K, V] :: H] = ServerHeaderMatchParamCons()
  def :>[K, V](server: TypeCarrier[ServerHeaderSendElement[K, V]]): ServerHeaderSendElCons[ServerHeaderSendElement[K, V] :: H] = ServerHeaderSendElCons()
}

/** Initial element with empty api description. */
case object EmptyCons extends PathOps[HNil] with HeaderOps[HNil] with MethodOps[HNil] with ApiList[HNil] {

  def :>[K, V](query: TypeCarrier[QueryParam[K, V]]): QueryCons[QueryParam[K, V] :: HNil] = QueryCons()
}

/** Last set element is a path. */
final case class PathCons[H <: HList]() extends PathOps[H] with HeaderOps[H] with MethodOps[H] with ApiList[H] {
  
  def :>[K, V](query: TypeCarrier[QueryParam[K, V]]): QueryCons[QueryParam[K, V] :: H] = QueryCons()
}

/** Last set element is a segment. */
final case class SegmentCons[H <: HList]() extends PathOps[H] with HeaderOps[H] with MethodOps[H] with ApiList[H] {

  def :>[K, V](query: TypeCarrier[QueryParam[K, V]]): QueryCons[QueryParam[K, V] :: H] = QueryCons()
}

/** Last set element is a query parameter. */
final case class QueryCons[H <: HList]() extends HeaderOps[H] with MethodOps[H] with ApiList[H] {

  def :>[K, V](query: TypeCarrier[QueryParam[K, V]]): QueryCons[QueryParam[K, V] :: H] = QueryCons()
}

/** Last set element is a header. */
sealed trait HeaderCons[H <: HList] extends HeaderOps[H] with MethodOps[H] with ApiList[H]

final case class InputHeaderCons[H <: HList]() extends HeaderCons[H]
final case class FixedHeaderCons[H <: HList]() extends HeaderCons[H]
final case class ClientHeaderElCons[H <: HList]() extends HeaderCons[H]
final case class ClientHeaderParamCons[H <: HList]() extends HeaderCons[H]
final case class ClientHeaderCollParamCons[H <: HList]() extends HeaderCons[H]
final case class ServerHeaderMatchParamCons[H <: HList]() extends HeaderCons[H]
final case class ServerHeaderSendElCons[H <: HList]() extends HeaderCons[H]

/** Last set element is a request body. */
final case class WithBodyCons[BMT <: MediaType, Bd, H <: HList]() extends ApiList[H] {

  def :>[M <: MethodElement](method: TypeCarrier[M])(implicit out: MethodToReqBody[M, BMT, Bd]): ApiTypeCarrier[out.Out :: H] = ApiTypeCarrier()
}
