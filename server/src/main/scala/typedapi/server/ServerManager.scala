package typedapi.server

final case class ServerManager[S](server: S, host: String, port: Int)

object ServerManager {

  def mount[S, Req, Resp, Out](server: ServerManager[S], endpoints: List[Serve[Req, Resp]])(implicit mounting: MountEndpoints.Aux[S, Req, Resp, Out]): Out =
    mounting(server, endpoints)
}

trait MountEndpoints[S, Req, Resp] {

  type Out

  def apply(server: ServerManager[S], endpoints: List[Serve[Req, Resp]]): Out
}

object MountEndpoints {

  type Aux[S, Req, Resp, Out0] = MountEndpoints[S, Req, Resp] { type Out = Out0 }
}
