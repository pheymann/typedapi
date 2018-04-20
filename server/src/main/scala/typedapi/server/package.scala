package typedapi

import typedapi.shared._
import shapeless._

import scala.language.higherKinds

package object server extends TypeLevelFoldLeftLowPrio 
                      with TypeLevelFoldLeftListLowPrio 
                      with ApiTransformer 
                      with FunctionApplyLowPrio 
                      with ValueExtractorInstances 
                      with RouteExtractorMediumPrio
                      with FoldResultEvidenceLowPrio
                      with ServeToListLowPrio
                      with PrecompileEndpointLowPrio
                      with MergeToEndpointLowPrio {

  def derive[H <: HList, Fold, El <: HList, KIn <: HList, VIn <: HList, ROut, Out](apiList: ApiTypeCarrier[H])
                                                                                  (implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil, HNil), Fold],
                                                                                            ev: FoldResultEvidence.Aux[Fold, El, KIn, VIn, Out],
                                                                                            extractor: RouteExtractor.Aux[El, KIn, VIn, HNil, ROut], 
                                                                                            funApply: FunctionApply[VIn, Out]): EndpointDefinition[El, KIn, VIn, ROut, funApply.Fun, Out] =
    new EndpointDefinition[El, KIn, VIn, ROut, funApply.Fun, Out](extractor, funApply)


  def mount[S, El <: HList, KIn <: HList, VIn <: HList, ROut, F[_], FOut, Req, Resp, Out](server: ServerManager[S], endpoint: Endpoint[El, KIn, VIn, ROut, F, FOut])
                                                                                         (implicit executor: EndpointExecutor.Aux[Req, El, KIn, VIn, ROut, F, FOut, Resp], mounting: MountEndpoints.Aux[S, Req, Resp, Out]): Out =
    mounting(server, List(new Serve[executor.R, executor.Out] {
      def apply(req: executor.R, eReq: EndpointRequest): Either[ExtractionError, executor.Out] = executor(req, eReq, endpoint)
    }))

  def derive[H <: HList, Fold <: HList](apiLists: CompositionCons[H])
                                       (implicit folder: TypeLevelFoldLeftList.Aux[H, Fold],
                                                 pre: PrecompileEndpoint[Fold]): EndpointCompositionDefinition[Fold, pre.Comp, pre.Out] =
    new EndpointCompositionDefinition[Fold, pre.Comp, pre.Out](pre)

  object endpointToServe extends Poly1 {

    implicit def default[El <: HList, KIn <: HList, VIn <: HList, ROut, F[_], FOut](implicit executor: EndpointExecutor[El, KIn, VIn, ROut, F, FOut]) = 
      at[Endpoint[El, KIn, VIn, ROut, F, FOut]] { endpoint =>
        new Serve[executor.R, executor.Out] {
          def apply(req: executor.R, eReq: EndpointRequest): Either[ExtractionError, executor.Out] = executor(req, eReq, endpoint)
        }
      }
  }

  def mount[S, End <: HList, Req, Resp, Out](server: ServerManager[S], end: End)(implicit toList: ServeToList[End, Req, Resp], mounting: MountEndpoints.Aux[S, Req, Resp, Out]): Out =
    mounting(server, toList(end))
}
