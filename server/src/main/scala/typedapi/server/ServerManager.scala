package typedapi.server

import scala.annotation.tailrec

final case class ServerManager[S](server: S, host: String, port: Int)

object ServerManager {

  def mount[S, Req, Resp, Out](server: ServerManager[S], endpoints: List[Serve[Req, Resp]])(implicit mounting: MountEndpoints.Aux[S, Req, Resp, Out]): Out =
    mounting(server, endpoints)
}

trait MountEndpoints[S, Req, Resp] {

  type Out

  @tailrec
  final def checkMethods(eps: List[Serve[Req, Resp]], eReq: EndpointRequest, agg: List[String]): List[String] = eps match {
    case collection.immutable.::(endpoint, tail) => endpoint.exists(eReq) match {
      case Some(method) => method :: agg
      case _            => checkMethods(tail, eReq, agg)
    }

    case Nil => agg
  }

  def apply(server: ServerManager[S], endpoints: List[Serve[Req, Resp]]): Out
}

object MountEndpoints {

  type Aux[S, Req, Resp, Out0] = MountEndpoints[S, Req, Resp] { type Out = Out0 }
}
