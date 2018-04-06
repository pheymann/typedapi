
import typedapi.shared._
import shapeless._
import shapeless.ops.hlist.Prepend

package object typedapi {

  val Path = PathListEmpty
  def Segment[A] = new SegmentHelper[A]

  val Queries = QueryListEmpty
  def Query[A]   = new QueryHelper[A]

  val Headers = HeaderListEmpty
  def Header[A]  = new HeaderHelper[A]
  val RawHeaders = RawHeadersParam

  def ReqBody[A] = ReqBodyElement[A]
  def Get[A] = GetElement[A]
  def Put[A] = PutElement[A]
  def Post[A] = PostElement[A]
  def Delete[A] = DeleteElement[A]

  def api[M <: MethodElement, P <: HList, Q <: HList, H <: HList, Prep <: HList, Api <: HList]
      (method: M, path: PathList[P] = Path, queries: QueryList[Q] = Queries, headers: HeaderList[H] = Headers)
      (implicit prepQP: Prepend.Aux[Q, P, Prep], prepH: Prepend.Aux[H, Prep, Api]): ApiTypeCarrier[M :: Api] = ApiTypeCarrier()
}
