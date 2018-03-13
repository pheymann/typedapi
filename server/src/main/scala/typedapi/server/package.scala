package typedapi

import typedapi.shared._
import shapeless._

package object server extends typedapi.shared.ops.ApiListOps with TypeLevelFoldLeftLowPrio with TypeLevelFoldLeftListLowPrio with ApiTransformer with EndpointFunctionLowPrio with ValueExtractorInstances with RouteExtractorMediumPrio with PreCompiledEndpointLowPrio with EndpointMergerLowPrio {

  def transform[H <: HList, Out](apiList: FinalCons[H])
                                (implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil), Out]): TypeLevelFoldLeft.Aux[H, (HNil, HNil), Out] = 
    folder

  def link[H <: HList, El <: HList, In <: HList, ROut, CIn <: HList, Fun, Out](transformed: TypeLevelFoldLeft.Aux[H, (HNil, HNil), (El, In, Out)])
                                                                              (implicit extractor: RouteExtractor.Aux[El, In, HNil, ROut], endpoint: EndpointFunction.Aux[In, CIn, Fun, Out])
      : EndpointDefinition[El, In, ROut, CIn, Fun, Out] =
    new EndpointDefinition(extractor, endpoint)

  def serve[R , El <: HList, In <: HList, ROut, CIn <: HList, Fun, FOut, Out0](endpoint: Endpoint[El, In, ROut, CIn, Fun, FOut])
                                                                              (implicit executor: EndpointExecutor.Aux[R, El, In, ROut, CIn, Fun, FOut, Out0]): List[Serve.Aux[R, Out0]] = List(new Serve[R] {
      type Out = Out0

      def apply(req: R, eReq: EndpointRequest): Option[Out0] = executor(req, eReq, endpoint)
    })

  def transform[H <: HList, In <: HList, Out <: HList](apiLists: CompositionCons[H])
                                                      (implicit folders: TypeLevelFoldLeftList.Aux[H, In, Out]): TypeLevelFoldLeftList.Aux[H, In, Out] =
    folders

  def link[H <: HList, In <: HList, Fold <: HList, Comp <: EndpointComposition, Pre <: HList, Out <: HList]
    (transformed: TypeLevelFoldLeftList.Aux[H, In, Fold])
    (implicit pre: PreCompiledEndpoint.Aux[Fold, Comp, Pre], merger: EndpointMerger.Aux[Comp, Pre, Out]): EndpointCompositionDefintion[Comp, Pre, Out] =
    new EndpointCompositionDefintion(pre.precompiled, merger)
}
