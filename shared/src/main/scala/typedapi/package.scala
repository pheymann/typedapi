
import typedapi.shared._
import shapeless._
import shapeless.ops.hlist.Prepend

package object typedapi extends MethodToReqBodyLowPrio with MethodToStringLowPrio with MediaTypes {

  val Root       = PathListEmpty
  def Segment[V] = new PairTypeFromWitnessKey[SegmentParam, V]

  val Queries   = QueryListBuilder[HNil]()
  val NoQueries = Queries

  val Headers      = HeaderListBuilder[HNil]()
  val NoHeaders    = Headers

  def ReqBody[MT <: MediaType, A] = TypeCarrier[ReqBodyElement[MT, A]]()
  def Get[MT <: MediaType, A]     = TypeCarrier[GetElement[MT, A]]()
  def Put[MT <: MediaType, A]     = TypeCarrier[PutElement[MT, A]]()
  def Post[MT <: MediaType, A]    = TypeCarrier[PostElement[MT, A]]()
  def Delete[MT <: MediaType, A]  = TypeCarrier[DeleteElement[MT, A]]()

  type Json  = `Application/json`
  type Plain = `Text/plain`

  def api[M <: MethodElement, P <: HList, Q <: HList, H <: HList, Prep <: HList, Api <: HList]
      (method: TypeCarrier[M], path: PathList[P] = Root, queries: QueryListBuilder[Q] = NoQueries, headers: HeaderListBuilder[H] = NoHeaders)
      (implicit prepQP: Prepend.Aux[Q, P, Prep], prepH: Prepend.Aux[H, Prep, Api]): ApiTypeCarrier[M :: Api] = ApiTypeCarrier()

  def apiWithBody[M <: MethodElement, P <: HList, Q <: HList, H <: HList, Prep <: HList, Api <: HList, BMT <: MediaType, Bd]
      (method: TypeCarrier[M], body: TypeCarrier[ReqBodyElement[BMT, Bd]], path: PathList[P] = Root, queries: QueryListBuilder[Q] = NoQueries, headers: HeaderListBuilder[H] = NoHeaders)
      (implicit prepQP: Prepend.Aux[Q, P, Prep], prepH: Prepend.Aux[H, Prep, Api], m: MethodToReqBody[M, BMT, Bd]): ApiTypeCarrier[m.Out :: Api] = ApiTypeCarrier()
}
