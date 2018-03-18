package typedapi.server

final case class ServerManager[S, Req, Resp](server: S, host: String, port: Int)

object ServerManager {

  def mount[S, Req, Resp, Out](server: ServerManager[S, Req, Resp], endpoints: List[Serve.Aux[Req, Resp]])(implicit mounting: MountEndpoints.Aux[S, Req, Resp, Out]): Out =
    mounting(server, endpoints)
}
