package typedapi.server

trait Serve[R] {

  type Out

  def apply(req: R, eReq: EndpointRequest): Option[Out]
}

object Serve {

  type Aux[R, Out0] = Serve[R] { type Out = Out0 }
}

trait MountEndpoints[S, Req, Resp] {

  type Out

  def apply(server: ServerManager[S, Req, Resp], endpoints: List[Serve.Aux[Req, Resp]]): Out
}

object MountEndpoints {

  type Aux[S, Req, Resp, Out0] = MountEndpoints[S, Req, Resp] { type Out = Out0 }
}
