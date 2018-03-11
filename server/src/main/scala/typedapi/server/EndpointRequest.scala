package typedapi.server

sealed trait EndpointRequest {

  def method: String
  def uri: List[String]
  def queries: Map[String, String]
  def headers: Map[String, String]

  def withUri(uri: List[String]): EndpointRequest
  def withHeaders(headers: Map[String, String]): EndpointRequest
}

final case class SimpleEndpointRequest(method: String, 
                                       uri: List[String], 
                                       queries: Map[String, String], 
                                       headers: Map[String, String]) extends EndpointRequest {

  def withUri(_uri: List[String]): EndpointRequest = this.copy(uri = _uri)
  def withHeaders(_headers: Map[String, String]): EndpointRequest = this.copy(headers = _headers)
}

final case class BodyEndpointRequest[Bd](method: String, 
                                         uri: List[String], 
                                         queries: Map[String, String], 
                                         headers: Map[String, String], 
                                         body: Bd) extends EndpointRequest {
  def withUri(_uri: List[String]): EndpointRequest = this.copy(uri = _uri)
  def withHeaders(_headers: Map[String, String]): EndpointRequest = this.copy(headers = _headers)
}
