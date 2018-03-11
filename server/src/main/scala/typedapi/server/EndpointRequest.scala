package typedapi.server

final case class EndpointRequest(method: String, uri: List[String], queries: Map[String, String], headers: Map[String, String])
