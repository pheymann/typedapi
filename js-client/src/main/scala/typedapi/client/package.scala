package typedapi.client

import typedapi.util._
import org.scalajs.dom.XMLHttpRequest
import org.scalajs.dom.ext.Ajax

import scala.concurrent.{Future, ExecutionContext}

package object js {

  private def renderQueries(queries: Map[String, List[String]]): String = 
    if (queries.nonEmpty)
      queries
        .map { case (key, values) => s"$key=${values.mkString(",")}" }
        .mkString("?", "&", "")
  else
    ""

  private def flatten[A](decoded: Future[Either[Exception, A]])(implicit ec: ExecutionContext): Future[A] = decoded.flatMap {
    case Right(a)    => Future.successful(a)
    case Left(error) => Future.failed(error)
  }

  implicit def rawGetRequest(implicit ec: ExecutionContext) = new RawGetRequest[Ajax.type, Future] {
    type Resp = XMLHttpRequest

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[Resp] =
      cm.client
        .get(
          url     = deriveUriString(cm, uri) + renderQueries(queries),
          headers = headers
        )
  }

  implicit def getRequest[A](implicit decoder: Decoder[Future, A], ec: ExecutionContext) = new GetRequest[Ajax.type, Future, A] {
    private val raw = rawGetRequest

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[A] =
      raw(uri, queries, headers, cm).flatMap(response => flatten(decoder(response.responseText)))
  }

  implicit def rawPutRequest(implicit ec: ExecutionContext) = new RawPutRequest[Ajax.type, Future] {
    type Resp = XMLHttpRequest

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[Resp] =
      cm.client
        .put(
          url     = deriveUriString(cm, uri) + renderQueries(queries),
          headers = headers
        )
  }

  implicit def putRequest[A](implicit decoder: Decoder[Future, A], ec: ExecutionContext) = new PutRequest[Ajax.type, Future, A] {
    private val raw = rawPutRequest

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[A] =
      raw(uri, queries, headers, cm).flatMap(response => flatten(decoder(response.responseText)))
  }

  implicit def rawPutBodyRequest[Bd](implicit encoder: Encoder[Future, Bd], ec: ExecutionContext) = new RawPutWithBodyRequest[Ajax.type, Future, Bd] {
    type Resp = XMLHttpRequest

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Ajax.type]): Future[Resp] =
      encoder(body).flatMap { encoded => 
        cm.client
          .put(
            url     = deriveUriString(cm, uri) + renderQueries(queries),
            headers = headers,
            data    = encoded
          )
      }
  }

  implicit def putBodyRequest[Bd, A](implicit encoder: Encoder[Future, Bd], decoder: Decoder[Future, A], ec: ExecutionContext) = new PutWithBodyRequest[Ajax.type, Future, Bd, A] {
    private val raw = rawPutBodyRequest[Bd]

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Ajax.type]): Future[A] =
      raw(uri, queries, headers, body, cm).flatMap(response => flatten(decoder(response.responseText)))
  }

  implicit def rawPostRequest(implicit ec: ExecutionContext) = new RawPostRequest[Ajax.type, Future] {
    type Resp = XMLHttpRequest

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[Resp] =
      cm.client
        .post(
          url     = deriveUriString(cm, uri) + renderQueries(queries),
          headers = headers
        )
  }

  implicit def postRequest[A](implicit decoder: Decoder[Future, A], ec: ExecutionContext) = new PostRequest[Ajax.type, Future, A] {
    private val raw = rawPostRequest

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[A] =
      raw(uri, queries, headers, cm).flatMap(response => flatten(decoder(response.responseText)))
  }

  implicit def rawPostBodyRequest[Bd](implicit encoder: Encoder[Future, Bd], ec: ExecutionContext) = new RawPostWithBodyRequest[Ajax.type, Future, Bd] {
    type Resp = XMLHttpRequest

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Ajax.type]): Future[Resp] =
      encoder(body).flatMap { encoded =>
        cm.client
          .post(
            url     = deriveUriString(cm, uri) + renderQueries(queries),
            headers = headers,
            data    = encoded
          )
      }
  }

  implicit def postBodyRequest[Bd, A](implicit encoder: Encoder[Future, Bd], decoder: Decoder[Future, A], ec: ExecutionContext) = new PostWithBodyRequest[Ajax.type, Future, Bd, A] {
    private val raw = rawPostBodyRequest[Bd]

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Ajax.type]): Future[A] =
      raw(uri, queries, headers, body, cm).flatMap(response => flatten(decoder(response.responseText)))
  }

  implicit def rawDeleteRequest(implicit ec: ExecutionContext) = new RawDeleteRequest[Ajax.type, Future] {
    type Resp = XMLHttpRequest

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[Resp] =
      cm.client
        .delete(
          url     = deriveUriString(cm, uri) + renderQueries(queries),
          headers = headers
        )
  }

  implicit def deleteRequest[A](implicit decoder: Decoder[Future, A], ec: ExecutionContext) = new DeleteRequest[Ajax.type, Future, A] {
    private val raw = rawDeleteRequest

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[A] =
      raw(uri, queries, headers, cm).flatMap(response => flatten(decoder(response.responseText)))
  }
}
