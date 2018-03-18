package typedapi

import typedapi.shared._
import shapeless.ops.hlist.{ Mapper, ToTraversable }
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
    (implicit executor: EndpointExecutor[El, In, ROut, CIn, F, FOut]): List[Serve.Aux[executor.R, executor.Out]] = List(new Serve[executor.R] {
      type R = executor.R
      type Out = executor.Out

      def apply(req: R, eReq: EndpointRequest): Option[Out] = executor(req, eReq, endpoint)
    })

  private object endpointToServe extends Poly1 {

    implicit def default[El <: HList, In <: HList, ROut, CIn <: HList, F[_], FOut](implicit executor: EndpointExecutor[El, In, ROut, CIn, F, FOut]) = at[Endpoint[El, In, ROut, CIn, F, FOut]] { endpoint =>
      new Serve[executor.R] {
        type R = executor.R
        type Out = executor.Out

        def apply(req: R, eReq: EndpointRequest): Option[Out] = executor(req, eReq, endpoint)
      }
    }
  }

  def serve[End <: HList, MOut <: HList, Req, Resp](comp: EndpointComposition[End])(implicit mapper: Mapper.Aux[endpointToServe.type, End, MOut], traverse: ToTraversable.Aux[MOut, List, Serve.Aux[Req, Resp]]) = 
    comp.endpoints.map(endpointToServe).toList
}
