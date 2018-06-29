package typedapi.server

import typedapi.shared.MethodType
import shapeless._
import shapeless.ops.hlist.Prepend
import akka.http.scaladsl.{HttpExt, Http}
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{Unmarshal, FromEntityUnmarshaller}
import akka.http.scaladsl.marshalling.{Marshal, ToEntityMarshaller}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink

import scala.collection.mutable.Builder
import scala.concurrent.{Future, ExecutionContext}
import scala.annotation.tailrec

package object akkahttp {

  implicit def noReqBodyExecutor[El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, FOut](implicit encoder: ToEntityMarshaller[FOut], ec: ExecutionContext) = 
    new NoReqBodyExecutor[El, KIn, VIn, M, Future, FOut] {
      type R   = HttpRequest
      type Out = Future[HttpResponse]

      def apply(req: R, eReq: EndpointRequest, endpoint: Endpoint[El, KIn, VIn, M, VIn, Future, FOut]): Either[ExtractionError, Out] = {
        extract(eReq, endpoint).map { extracted =>
          execute(extracted, endpoint).flatMap { response =>
            Marshal(response).to[ResponseEntity].map { marshalledBody =>
              HttpResponse(entity = marshalledBody)
            }
          }
        }
      }
    }

  implicit def withReqBodyExecutor[El <: HList, KIn <: HList, VIn <: HList, Bd, M <: MethodType, ROut <: HList, POut <: HList, FOut]
    (implicit encoder: ToEntityMarshaller[FOut], 
              decoder: FromEntityUnmarshaller[Bd],
              _prepend: Prepend.Aux[ROut, Bd :: HNil, POut], 
              _eqProof: POut =:= VIn,
              mat: Materializer,
              ec: ExecutionContext) = new ReqBodyExecutor[El, KIn, VIn, Bd, M, ROut, POut, Future, FOut] {
    type R   = HttpRequest
    type Out = Future[HttpResponse]

    implicit val prepend = _prepend
    implicit val eqProof = _eqProof

    def apply(req: R, eReq: EndpointRequest, endpoint: Endpoint[El, KIn, VIn, M, (BodyType[Bd], ROut), Future, FOut]): Either[ExtractionError, Out] = {
      extract(eReq, endpoint).map { case (_, extracted) =>
        for {
          body     <- Unmarshal(req.entity).to[Bd]
          response <- execute(extracted, body, endpoint)

          entity <- Marshal(response).to[ResponseEntity]
        } yield HttpResponse(entity = entity)
      }
    }
  }

  implicit def mountEndpoints(implicit mat: Materializer) = new MountEndpoints[HttpExt, HttpRequest, Future[HttpResponse]] {
    type Out = Future[Http.ServerBinding]

    def apply(server: ServerManager[HttpExt], endpoints: List[Serve[HttpRequest, Future[HttpResponse]]]): Out = {
      val service: HttpRequest => Future[HttpResponse] = request => {
        def execute(eps: List[Serve[HttpRequest, Future[HttpResponse]]], eReq: EndpointRequest): Future[HttpResponse] = eps match {
          case collection.immutable.::(endpoint, tail) => endpoint(request, eReq) match {
            case Right(response)            => response
            case Left(RouteNotFound)        => execute(tail, eReq)
            case Left(BadRouteRequest(msg)) => Future.successful(HttpResponse(400, entity = msg))
          }

          case Nil => Future.successful(HttpResponse(404, entity = "uri = " + request.uri))
        }

        @tailrec
        def toListPath(path: Uri.Path, agg: Builder[String, List[String]]): List[String] = path match {
          case Uri.Path.Slash(tail) => toListPath(tail, agg)
          case Uri.Path.Segment(p, tail) => toListPath(tail, agg += p)
          case Uri.Path.Empty => agg.result()
        }

        val eReq = EndpointRequest(
          request.method.name,
          toListPath(request.uri.path, List.newBuilder),
          request.uri.query().toMultiMap,
          request.headers.toList.map(header => header.name.toString -> header.value)(collection.breakOut)
        )
        execute(endpoints, eReq)
      }
    
      server.server.bind(server.host, server.port).to(Sink.foreach { connection =>
        connection.handleWithAsyncHandler(service)
      }).run()
    }
  }
}
