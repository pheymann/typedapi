package typedapi

import typedapi.shared._
import shapeless.ops.hlist.Mapper
import shapeless._

import scala.language.higherKinds

package object server extends typedapi.shared.ops.ApiListOps 
with TypeLevelFoldLeftLowPrio 
with TypeLevelFoldLeftListLowPrio 
with ApiTransformer 
with FunctionDefLowPrio 
with ValueExtractorInstances 
with RouteExtractorMediumPrio
with FoldResultEvidenceLowPrio {

  def transform[H <: HList, Out](apiList: FinalCons[H])
                                (implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil), Out]): TypeLevelFoldLeft.Aux[H, (HNil, HNil), Out] = 
    folder

  def link[H <: HList, Fold, El <: HList, In <: HList, ROut, Out](apiList: FinalCons[H])
                                                                                    (implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil), Fold],
ev: FoldResultEvidence.Aux[Fold, El, In, Out],
extractor: RouteExtractor.Aux[El, In, HNil, ROut], 
funDef: FunctionDef[In, Out])
      : EndpointDefinition[El, In, ROut, funDef.CIn, funDef.Fun, Out] =
    new EndpointDefinition[El, In, ROut, funDef.CIn, funDef.Fun, Out](extractor, funDef)


  def serve[El <: HList, In <: HList, ROut, CIn <: HList, F[_], FOut](endpoint: Endpoint[El, In, ROut, CIn, F, FOut])
    (implicit executor: EndpointExecutor[El, In, ROut, CIn, F, FOut]): List[Serve[executor.R, executor.Out]] = List(new Serve[executor.R, executor.Out] {
      def apply(req: executor.R, eReq: EndpointRequest): Option[executor.Out] = executor(req, eReq, endpoint)
    })

  private object endpointToServe extends Poly1 {

    implicit def default[El <: HList, In <: HList, ROut, CIn <: HList, F[_], FOut](implicit executor: EndpointExecutor[El, In, ROut, CIn, F, FOut]) = at[Endpoint[El, In, ROut, CIn, F, FOut]] { endpoint =>
      new Serve[executor.R, executor.Out] {
        def apply(req: executor.R, eReq: EndpointRequest): Option[executor.Out] = executor(req, eReq, endpoint)
      }
    }
  }

  trait ServeToList[H <: HList, Req, Resp] {

    def apply(h: H): List[Serve[Req, Resp]]
  }

  implicit def hnilToList[Req, Resp] = new ServeToList[HNil, Req, Resp] {
    def apply(h: HNil): List[Serve[Req, Resp]] = Nil
  }

  implicit def serveToList[Req, Resp, T <: HList](implicit next: ServeToList[T, Req, Resp]) = new ServeToList[Serve[Req, Resp] :: T, Req, Resp] {
    def apply(h: Serve[Req, Resp] :: T): List[Serve[Req, Resp]] = h.head :: next(h.tail)
  }

  def serve[End <: HList, MOut <: HList, Req, Resp](comp: EndpointComposition[End])(implicit mapper: Mapper.Aux[endpointToServe.type, End, MOut], toList: ServeToList[MOut, Req, Resp]): List[Serve[Req, Resp]] = 
    toList(comp.endpoints.map(endpointToServe))
}
