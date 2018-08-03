package typedapi.server

import scala.annotation.tailrec

final case class ServerManager[S](server: S, host: String, port: Int)

object ServerManager {

  def mount[S, Req, Resp, Out](server: ServerManager[S], endpoints: List[Serve[Req, Resp]])(implicit mounting: MountEndpoints.Aux[S, Req, Resp, Out]): Out =
    mounting(server, endpoints)
}

trait MountEndpoints[S, Req, Resp] {

  type Out

  final def optionsHeaders(eps: List[Serve[Req, Resp]], eReq: EndpointRequest): Map[String, String] = {
    @tailrec
    def collect(serve: List[Serve[Req, Resp]], methods: List[String], headers: Map[String, String]): (List[String], Map[String, String]) = serve match {
      case collection.immutable.::(endpoint, tail) => endpoint.options(eReq) match {
        case Some((method, hds)) => collect(tail, method :: methods, hds ++ headers)
        case _                   => collect(tail, methods, headers)
      }

      case Nil => (methods, headers)
    }

    val (methods, headers) = collect(eps, Nil, Map.empty)
    
    headers + (("Access-Control-Allow-Methods", methods.mkString(",")))
  }

  def apply(server: ServerManager[S], endpoints: List[Serve[Req, Resp]]): Out
}

object MountEndpoints {

  type Aux[S, Req, Resp, Out0] = MountEndpoints[S, Req, Resp] { type Out = Out0 }
}
