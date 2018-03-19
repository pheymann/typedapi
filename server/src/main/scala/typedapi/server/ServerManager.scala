package typedapi.server

import shapeless.HList
import shapeless.ops.hlist.Mapper

final case class ServerManager[S](server: S, host: String, port: Int)

object ServerManager {

  def mount[S, Req, Resp, Out](server: ServerManager[S], endpoints: List[Serve[Req, Resp]])(implicit mounting: MountEndpoints.Aux[S, Req, Resp, Out]): Out =
    mounting(server, endpoints)

  def mount1[S, Req, Resp, End <: HList, MOut <: HList, Out](server: ServerManager[S], comp: EndpointComposition[End])
                                                           (implicit mapper: Mapper.Aux[endpointToServe.type, End, MOut], 
                                                                     toList: ServeToList[MOut, Req, Resp],
                                                                     mounting: MountEndpoints.Aux[S, Req, Resp, Out]): Out = 
    mounting(server, serve[End, MOut, Req, Resp](comp))
}

trait MountEndpoints[S, Req, Resp] {

  type Out

  def apply(server: ServerManager[S], endpoints: List[Serve[Req, Resp]]): Out
}

object MountEndpoints {

  type Aux[S, Req, Resp, Out0] = MountEndpoints[S, Req, Resp] { type Out = Out0 }
}
