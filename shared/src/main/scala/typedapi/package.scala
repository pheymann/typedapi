
import typedapi.shared._
import shapeless._
import shapeless.ops.hlist.Prepend

package object typedapi extends MethodToReqBodyLowPrio {

  val Root       = PathListEmpty
  def Segment[A] = new SegmentHelper[A]

  val Queries   = QueryListEmpty
  val NoQueries = Queries
  def Query[A]  = new QueryHelper[A]

  val Headers    = HeaderListEmpty
  val NoHeaders  = Headers
  def Header[A]  = new HeaderHelper[A]
  val RawHeaders = RawHeadersParam

  def ReqBody[A] = ReqBodyElement[A]
  def Get[A]     = GetElement[A]
  def Put[A]     = PutElement[A]
  def Post[A]    = PostElement[A]
  def Delete[A]  = DeleteElement[A]

  def api[M <: MethodElement, P <: HList, Q <: HList, H <: HList, Prep <: HList, Api <: HList]
      (method: M, path: PathList[P] = Root, queries: QueryList[Q] = NoQueries, headers: HeaderList[H] = NoHeaders)
      (implicit prepQP: Prepend.Aux[Q, P, Prep], prepH: Prepend.Aux[H, Prep, Api]): ApiTypeCarrier[M :: Api] = ApiTypeCarrier()

  def apiWithBody[M <: MethodElement, P <: HList, Q <: HList, H <: HList, Prep <: HList, Api <: HList, Bd]
      (method: M, body: ReqBodyElement[Bd], path: PathList[P] = Root, queries: QueryList[Q] = NoQueries, headers: HeaderList[H] = NoHeaders)
      (implicit prepQP: Prepend.Aux[Q, P, Prep], prepH: Prepend.Aux[H, Prep, Api], m: MethodToReqBody[M, Bd]): ApiTypeCarrier[m.Out :: Api] = ApiTypeCarrier()
}
