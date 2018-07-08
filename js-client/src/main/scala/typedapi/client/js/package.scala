package typedapi.client

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
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[A] = {
      cm.client
        .get(
          url     = deriveUriString(cm, uri) + renderQueries(queries),
          headers = headers
        )
        .flatMap(response => flatten(decoder(response.responseText)))
    }
  }

  implicit def putRequest[A](implicit decoder: Decoder[Future, A], ec: ExecutionContext) = new PutRequest[Ajax.type, Future, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[A] = {
      cm.client
        .put(
          url     = deriveUriString(cm, uri) + renderQueries(queries),
          headers = headers
        )
        .flatMap(response => flatten(decoder(response.responseText)))
    }
  }

  implicit def putBodyRequest[Bd, A](implicit encoder: Encoder[Future, Bd], decoder: Decoder[Future, A], ec: ExecutionContext) = new PutWithBodyRequest[Ajax.type, Future, Bd, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Ajax.type]): Future[A] = {
      encoder(body).flatMap { encoded => 
        cm.client
          .put(
            url     = deriveUriString(cm, uri) + renderQueries(queries),
            headers = headers,
            data    = encoded
          )
          .flatMap(response => flatten(decoder(response.responseText)))
      }
    }
  }

  implicit def postRequest[A](implicit decoder: Decoder[Future, A], ec: ExecutionContext) = new PostRequest[Ajax.type, Future, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[A] = {
      cm.client
        .post(
          url     = deriveUriString(cm, uri) + renderQueries(queries),
          headers = headers
        )
        .flatMap(response => flatten(decoder(response.responseText)))
    }
  }

  implicit def postBodyRequest[Bd, A](implicit encoder: Encoder[Future, Bd], decoder: Decoder[Future, A], ec: ExecutionContext) = new PostWithBodyRequest[Ajax.type, Future, Bd, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Ajax.type]): Future[A] = {
      encoder(body).flatMap { encoded =>
        cm.client
          .post(
            url     = deriveUriString(cm, uri) + renderQueries(queries),
            headers = headers,
            data    = encoded
          )
          .flatMap(response => flatten(decoder(response.responseText)))
      }
    }
  }

  implicit def deleteRequest[A](implicit decoder: Decoder[Future, A], ec: ExecutionContext) = new DeleteRequest[Ajax.type, Future, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Ajax.type]): Future[A] = {
      cm.client
        .delete(
          url     = deriveUriString(cm, uri) + renderQueries(queries),
          headers = headers
        )
        .flatMap(response => flatten(decoder(response.responseText)))
    }
  }
}
