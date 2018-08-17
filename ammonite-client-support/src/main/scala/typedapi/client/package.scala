package typedapi.client

import typedapi.util._
import scalaj.http._

package object amm {

  type Id[A]       = scalajhttp.Id[A]
  type Blocking[A] = scalajhttp.Blocking[A]

  def clientManager(host: String, port: Int): ClientManager[Http.type] = ClientManager(Http, host, port)

  implicit def rawGetRequestAmm = scalajhttp.rawGetRequest
  implicit def getRequestAmm[A](implicit decoder: Decoder[Id, A]) = scalajhttp.getRequest[A]

  implicit def rawPutRequestAmm = scalajhttp.rawPutRequest
  implicit def putRequestAmm[A](implicit decoder: Decoder[Id, A]) = scalajhttp.putRequest[A]

  implicit def rawPutBodyRequestAmm[Bd](implicit encoder: Encoder[Id, Bd]) = scalajhttp.rawPutBodyRequest[Bd]
  implicit def putBodyRequestAmm[Bd, A](implicit encoder: Encoder[Id, Bd], decoder: Decoder[Id, A]) = scalajhttp.putBodyRequest[Bd, A]

  implicit def rawPostRequestAmm = scalajhttp.rawPostRequest
  implicit def postRequest[A](implicit decoder: Decoder[Id, A]) = scalajhttp.postRequest[A]

  implicit def rawPostBodyRequestAm[Bd](implicit encoder: Encoder[Id, Bd]) = scalajhttp.rawPostBodyRequest[Bd]
  implicit def postBodyRequestAmm[Bd, A](implicit encoder: Encoder[Id, Bd], decoder: Decoder[Id, A]) = scalajhttp.postBodyRequest[Bd, A]

  implicit def rawDeleteRequestAmm = scalajhttp.rawDeleteRequest
  implicit def deleteRequestAmm[A](implicit decoder: Decoder[Id, A]) = scalajhttp.deleteRequest[A]
}
