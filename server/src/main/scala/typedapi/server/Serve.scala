package typedapi.server

/** Reduces an Endpoint and its EndpointExecutor to a simple Request => Response function. */
trait Serve[Req, Resp] {

  def options(eReq: EndpointRequest): Option[(String, Map[String, String])]
  def apply(req: Req, eReq: EndpointRequest): Either[ExtractionError, Resp]
}
