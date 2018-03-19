package typedapi

import typedapi.shared._
import shapeless.ops.hlist.Mapper
import shapeless._

import scala.language.higherKinds

package object server extends typedapi.shared.ops.ApiListOps 
                      with TypeLevelFoldLeftLowPrio 
                      with TypeLevelFoldLeftListLowPrio 
                      with ApiTransformer 
                      with FunctionApplyLowPrio 
                      with ValueExtractorInstances 
                      with RouteExtractorMediumPrio
                      with FoldResultEvidenceLowPrio
                      with ServeToListLowPrio {

  def link[H <: HList, Fold, El <: HList, In <: HList, ROut, Out](apiList: FinalCons[H])
                                                                 (implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil), Fold],
                                                                           ev: FoldResultEvidence.Aux[Fold, El, In, Out],
                                                                           extractor: RouteExtractor.Aux[El, In, HNil, ROut], 
                                                                           funApply: FunctionApply[In, Out]): EndpointDefinition[El, In, ROut, funApply.CIn, funApply.Fun, Out] =
    new EndpointDefinition[El, In, ROut, funApply.CIn, funApply.Fun, Out](extractor, funApply)


  def serve[El <: HList, In <: HList, ROut, CIn <: HList, F[_], FOut](endpoint: Endpoint[El, In, ROut, CIn, F, FOut])
                                                                     (implicit executor: EndpointExecutor[El, In, ROut, CIn, F, FOut]): List[Serve[executor.R, executor.Out]] = 
    List(new Serve[executor.R, executor.Out] {
      def apply(req: executor.R, eReq: EndpointRequest): Option[executor.Out] = executor(req, eReq, endpoint)
    })

  object endpointToServe extends Poly1 {

    implicit def default[El <: HList, In <: HList, ROut, CIn <: HList, F[_], FOut](implicit executor: EndpointExecutor[El, In, ROut, CIn, F, FOut]) = 
      at[Endpoint[El, In, ROut, CIn, F, FOut]] { endpoint =>
        new Serve[executor.R, executor.Out] {
          def apply(req: executor.R, eReq: EndpointRequest): Option[executor.Out] = executor(req, eReq, endpoint)
        }
      }
  }

  def serve[End <: HList, MOut <: HList, Req, Resp](comp: EndpointComposition[End])
                                                   (implicit mapper: Mapper.Aux[endpointToServe.type, End, MOut], 
                                                             toList: ServeToList[MOut, Req, Resp]): List[Serve[Req, Resp]] = 
    toList(comp.endpoints.map(endpointToServe))
}
