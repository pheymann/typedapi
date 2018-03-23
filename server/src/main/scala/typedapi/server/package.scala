package typedapi

import typedapi.shared._
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
                      with ServeToListLowPrio
                      with PrecompileEndpointLowPrio
                      with MergeToEndpointLowPrio {

  def link[H <: HList, Fold, El <: HList, In <: HList, ROut, Out](apiList: FinalCons[H])
                                                                 (implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil), Fold],
                                                                           ev: FoldResultEvidence.Aux[Fold, El, In, Out],
                                                                           extractor: RouteExtractor.Aux[El, In, HNil, ROut], 
                                                                           funApply: FunctionApply[In, Out]): EndpointDefinition[El, In, ROut, funApply.CIn, funApply.Fun, Out] =
    new EndpointDefinition[El, In, ROut, funApply.CIn, funApply.Fun, Out](extractor, funApply)


  def mount[S, El <: HList, In <: HList, ROut, CIn <: HList, F[_], FOut, Req, Resp, Out](server: ServerManager[S], endpoint: Endpoint[El, In, ROut, CIn, F, FOut])
                                                                     (implicit executor: EndpointExecutor.Aux[Req, El, In, ROut, CIn, F, FOut, Resp], mounting: MountEndpoints.Aux[S, Req, Resp, Out]): Out =
    mounting(server, List(new Serve[executor.R, executor.Out] {
      def apply(req: executor.R, eReq: EndpointRequest): Either[ExtractionError, executor.Out] = executor(req, eReq, endpoint)
    }))

  def link[H <: HList, Fold <: HList](apiLists: CompositionCons[H])
                                     (implicit folder: TypeLevelFoldLeftList.Aux[H, Fold],
                                               pre: PrecompileEndpoint[Fold]): EndpointCompositionDefinition[Fold, pre.Comp, pre.Out] =
    new EndpointCompositionDefinition[Fold, pre.Comp, pre.Out](pre)

  object endpointToServe extends Poly1 {

    implicit def default[El <: HList, In <: HList, ROut, CIn <: HList, F[_], FOut](implicit executor: EndpointExecutor[El, In, ROut, CIn, F, FOut]) = 
      at[Endpoint[El, In, ROut, CIn, F, FOut]] { endpoint =>
        new Serve[executor.R, executor.Out] {
          def apply(req: executor.R, eReq: EndpointRequest): Either[ExtractionError, executor.Out] = executor(req, eReq, endpoint)
        }
      }
  }

  def mount[S, End <: HList, Req, Resp, Out](server: ServerManager[S], end: End)(implicit toList: ServeToList[End, Req, Resp], mounting: MountEndpoints.Aux[S, Req, Resp, Out]): Out =
    mounting(server, toList(end))
}
