package typedapi

import typedapi.shared._
import shapeless._
import shapeless.ops.hlist.Mapper

import scala.language.higherKinds

package object server extends TypeLevelFoldLeftLowPrio 
                      with TypeLevelFoldLeftListLowPrio 
                      with ApiTransformer {

  def derive[F[_]]: ExecutableDerivation[F] = new ExecutableDerivation[F]


  def mount[S, El <: HList, KIn <: HList, VIn <: HList, M <: MethodCall, ROut, F[_], FOut, Req, Resp, Out](server: ServerManager[S], endpoint: Endpoint[El, KIn, VIn, M, ROut, F, FOut])
                                                                                                          (implicit executor: EndpointExecutor.Aux[Req, El, KIn, VIn, M, ROut, F, FOut, Resp], mounting: MountEndpoints.Aux[S, Req, Resp, Out]): Out =
    mounting(server, List(new Serve[executor.R, executor.Out] {
      def apply(req: executor.R, eReq: EndpointRequest): Either[ExtractionError, executor.Out] = executor(req, eReq, endpoint)
    }))

  def deriveAll[F[_]]: ExecutableCompositionDerivation[F] = new ExecutableCompositionDerivation[F]

  object endpointToServe extends Poly1 {

    implicit def default[El <: HList, KIn <: HList, VIn <: HList, M <: MethodCall, ROut, F[_], FOut](implicit executor: EndpointExecutor[El, KIn, VIn, M, ROut, F, FOut]) = 
      at[Endpoint[El, KIn, VIn, M, ROut, F, FOut]] { endpoint =>
        new Serve[executor.R, executor.Out] {
          def apply(req: executor.R, eReq: EndpointRequest): Either[ExtractionError, executor.Out] = executor(req, eReq, endpoint)
        }
      }
  }

  def mount[S, End <: HList, Serv <: HList, Req, Resp, Out](server: ServerManager[S], end: End)(implicit mapper: Mapper.Aux[endpointToServe.type, End, Serv], toList: ServeToList[Serv, Req, Resp], mounting: MountEndpoints.Aux[S, Req, Resp, Out]): Out =
    mounting(server, toList(end.map(endpointToServe)))
}
