package typedapi.server

trait Serve[Req, Resp] {

  def apply(req: Req, eReq: EndpointRequest): Either[ExtractionError, Resp]
}
