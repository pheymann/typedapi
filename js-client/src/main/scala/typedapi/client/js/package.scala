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

  implicit def getRequest[A](implicit decoder: Decoder[Future, A], ec: ExecutionContext) = new GetRequest[Ajax.type, Future, A] {
    type Resp = XMLHttpRequest

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[Resp] =
      cm.client
        .get(
          url     = deriveUriString(cm, uri) + renderQueries(queries),
          headers = headers
        )

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[A] =
      raw(uri, queries, headers, cm).flatMap(response => flatten(decoder(response.responseText)))
  }

  implicit def putRequest[A](implicit decoder: Decoder[Future, A], ec: ExecutionContext) = new PutRequest[Ajax.type, Future, A] {
    type Resp = XMLHttpRequest

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[Resp] =
      cm.client
        .put(
          url     = deriveUriString(cm, uri) + renderQueries(queries),
          headers = headers
        )

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[A] =
      raw(uri, queries, headers, cm).flatMap(response => flatten(decoder(response.responseText)))
  }

  implicit def putBodyRequest[Bd, A](implicit encoder: Encoder[Future, Bd], decoder: Decoder[Future, A], ec: ExecutionContext) = new PutWithBodyRequest[Ajax.type, Future, Bd, A] {
    type Resp = XMLHttpRequest

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Ajax.type]): Future[Resp] =
      encoder(body).flatMap { encoded => 
        cm.client
          .put(
            url     = deriveUriString(cm, uri) + renderQueries(queries),
            headers = headers,
            data    = encoded
          )
      }

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Ajax.type]): Future[A] =
      raw(uri, queries, headers, body, cm).flatMap(response => flatten(decoder(response.responseText)))
  }

  implicit def postRequest[A](implicit decoder: Decoder[Future, A], ec: ExecutionContext) = new PostRequest[Ajax.type, Future, A] {
    type Resp = XMLHttpRequest

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[Resp] =
      cm.client
        .post(
          url     = deriveUriString(cm, uri) + renderQueries(queries),
          headers = headers
        )

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[A] =
      raw(uri, queries, headers, cm).flatMap(response => flatten(decoder(response.responseText)))
  }

  implicit def postBodyRequest[Bd, A](implicit encoder: Encoder[Future, Bd], decoder: Decoder[Future, A], ec: ExecutionContext) = new PostWithBodyRequest[Ajax.type, Future, Bd, A] {
    type Resp = XMLHttpRequest

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Ajax.type]): Future[Resp] =
      encoder(body).flatMap { encoded =>
        cm.client
          .post(
            url     = deriveUriString(cm, uri) + renderQueries(queries),
            headers = headers,
            data    = encoded
          )
      }

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Ajax.type]): Future[A] =
      raw(uri, queries, headers, body, cm).flatMap(response => flatten(decoder(response.responseText)))
  }

  implicit def deleteRequest[A](implicit decoder: Decoder[Future, A], ec: ExecutionContext) = new DeleteRequest[Ajax.type, Future, A] {
    type Resp = XMLHttpRequest

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[Resp] =
      cm.client
        .delete(
          url     = deriveUriString(cm, uri) + renderQueries(queries),
          headers = headers
        )

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[A] =
      raw(uri, queries, headers, cm).flatMap(response => flatten(decoder(response.responseText)))
  }
}
