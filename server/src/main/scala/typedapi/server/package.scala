package typedapi

import shapeless.ops.hlist.{ Mapper, ToTraversable }
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

  def serve[Req , El <: HList, In <: HList, ROut, CIn <: HList, Fun, FOut, Resp](endpoint: Endpoint[El, In, ROut, CIn, Fun, FOut])
                                                                                (implicit executor: EndpointExecutor.Aux[Req, El, In, ROut, CIn, Fun, FOut, Resp]): List[Serve.Aux[Req, Resp]] = List(new Serve[Req] {
      type Out = Resp

      def apply(req: Req, eReq: EndpointRequest): Option[Resp] = executor(req, eReq, endpoint)
    })

  def transform[H <: HList, In <: HList, Out <: HList](apiLists: CompositionCons[H])
                                                      (implicit folders: TypeLevelFoldLeftList.Aux[H, In, Out]): TypeLevelFoldLeftList.Aux[H, In, Out] =
    folders

  def link[H <: HList, In <: HList, Fold <: HList, Comp <: EndpointComposition, Pre <: HList, Out <: HList]
    (transformed: TypeLevelFoldLeftList.Aux[H, In, Fold])
    (implicit pre: PreCompiledEndpoint.Aux[Fold, Comp, Pre], merger: EndpointMerger.Aux[Comp, Pre, Out]): EndpointCompositionDefintion[Comp, Pre, Out] =
    new EndpointCompositionDefintion(pre.precompiled, merger)

  private object endpointToServe extends Poly1 {

    implicit def default[R , El <: HList, In <: HList, ROut, CIn <: HList, Fun, FOut, Out0](implicit executor: EndpointExecutor.Aux[R, El, In, ROut, CIn, Fun, FOut, Out0]) = at[Endpoint[El, In, ROut, CIn, Fun, FOut]] { endpoint =>
      new Serve[R] {
        type Out = Out0

        def apply(req: R, eReq: EndpointRequest): Option[Out0] = executor(req, eReq, endpoint)
      }
    }
  }

  def serve[End <: HList, MOut <: HList, Req, Resp](endpoints: End)(implicit mapper: Mapper.Aux[endpointToServe.type, End, MOut], traverse: ToTraversable.Aux[MOut, List, Serve.Aux[Req, Resp]]) = 
    endpoints.map(endpointToServe).toList
}
