package typedapi.client

import cats.MonadError
import org.http4s._
import org.http4s.client._

import scala.language.higherKinds

package object http4s {

  private implicit class TypedApiRequestOps[F[_]](req: Request[F]) {

    def withQuery(queries: Map[String, List[String]]): Request[F] = {
      if (queries.nonEmpty) {
        val q   = org.http4s.Query.fromMap(queries)
        val uri = Uri(req.uri.scheme, req.uri.authority, req.uri.path, q, req.uri.fragment)

        req.withUri(uri)
      }
      else req
    }

    def withHeaders(headers: Map[String, String]): Request[F] = {
      if (headers.nonEmpty) {
        val h: List[Header] = headers.map { case (k, v) => org.http4s.Header(k, v) }(collection.breakOut)

        req.withHeaders(Headers(h))
      }
      else req
    }
  }

  implicit def getRequest[F[_], A](implicit decoder: EntityDecoder[F, A], F: MonadError[F, Throwable]) = new GetRequest[Client[F], F, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Client[F]]): F[A] = {
      val request = Request[F](Method.GET, Uri.unsafeFromString(deriveUriString(cm, uri)))
        .withQuery(queries)
        .withHeaders(headers)

      cm.client.expect[A](request)
    }
  }

  implicit def putRequest[F[_], A](implicit decoder: EntityDecoder[F, A], F: MonadError[F, Throwable]) = new PutRequest[Client[F], F, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Client[F]]): F[A] = {
      val request = Request[F](Method.PUT, Uri.unsafeFromString(deriveUriString(cm, uri)))
        .withQuery(queries)
        .withHeaders(headers)

      cm.client.expect[A](request)
    }
  }

  implicit def putBodyRequest[F[_], Bd, A](implicit encoder: EntityEncoder[F, Bd], 
                                                    decoder: EntityDecoder[F, A], 
                                                    F: MonadError[F, Throwable]) = new PutWithBodyRequest[Client[F], F, Bd, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Client[F]]): F[A] = {
      val request = Request[F](Method.PUT, Uri.unsafeFromString(deriveUriString(cm, uri)))
        .withQuery(queries)
        .withHeaders(headers)
        .withBody(body)

      cm.client.expect[A](request)
    }
  }

  implicit def postRequest[F[_], A](implicit decoder: EntityDecoder[F, A], F: MonadError[F, Throwable]) = new PostRequest[Client[F], F, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Client[F]]): F[A] = {
      val request = Request[F](Method.POST, Uri.unsafeFromString(deriveUriString(cm, uri)))
        .withQuery(queries)
        .withHeaders(headers)

      cm.client.expect[A](request)
    }
  }

  implicit def postBodyRequest[F[_], Bd, A](implicit encoder: EntityEncoder[F, Bd], 
                                                     decoder: EntityDecoder[F, A], 
                                                     F: MonadError[F, Throwable]) = new PostWithBodyRequest[Client[F], F, Bd, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Client[F]]): F[A] = {
      val request = Request[F](Method.POST, Uri.unsafeFromString(deriveUriString(cm, uri)))
        .withQuery(queries)
        .withHeaders(headers)
        .withBody(body)

      cm.client.expect[A](request)
    }
  }

  implicit def deleteRequest[F[_], A](implicit decoder: EntityDecoder[F, A], F: MonadError[F, Throwable]) = new DeleteRequest[Client[F], F, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Client[F]]): F[A] = {
      val request = Request[F](Method.DELETE, Uri.unsafeFromString(deriveUriString(cm, uri)))
        .withQuery(queries)
        .withHeaders(headers)

      cm.client.expect[A](request)
    }
  }
}
