package typedapi.client

import cats.{MonadError, Applicative}
import org.http4s._
import org.http4s.client._
import org.http4s.Status.Successful
import org.http4s.headers.{Accept, MediaRangeAndQValue}

import scala.language.higherKinds

package object http4s {

  private implicit class Http4sRequestOps[F[_]](req: Request[F]) {

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

    def run[A](cm: ClientManager[Client[F]])(implicit d: EntityDecoder[F, A], F: Applicative[F]): F[Response[F]] = {
      val r = 
        if (d.consumes.nonEmpty) {
          val m = d.consumes.toList
          req.putHeaders(Accept(MediaRangeAndQValue(m.head), m.tail.map(MediaRangeAndQValue(_)): _*))
        } 
        else req

      cm.client.fetch(r)(resp => F.pure(resp))
    }
  }

  private implicit class Http4sResponseOps[F[_]](resp: Response[F]) {

    def decode[A](implicit d: EntityDecoder[F, A], F: MonadError[F, Throwable]): F[A] = resp match {
      case Successful(_resp) =>
        d.decode(_resp, strict = false).fold(throw _, identity)
      case failedResponse =>
        F.raiseError(UnexpectedStatus(failedResponse.status))
    }
  }

  implicit def getRequest[F[_], A](implicit decoder: EntityDecoder[F, A], F: MonadError[F, Throwable]) = new GetRequest[Client[F], F, A] {
    type Resp = Response[F]

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Client[F]]): F[Resp] = {
      val request = Request[F](Method.GET, Uri.unsafeFromString(deriveUriString(cm, uri)))
        .withQuery(queries)
        .withHeaders(headers)

      request.run[A](cm)
    }

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Client[F]]): F[A] =
      F.flatMap(raw(uri, queries, headers, cm))(_.decode[A])
  }

  implicit def putRequest[F[_], A](implicit decoder: EntityDecoder[F, A], F: MonadError[F, Throwable]) = new PutRequest[Client[F], F, A] {
    type Resp = Response[F]

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Client[F]]): F[Resp] = {
      val request = Request[F](Method.PUT, Uri.unsafeFromString(deriveUriString(cm, uri)))
        .withQuery(queries)
        .withHeaders(headers)

      request.run[A](cm)
    }

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Client[F]]): F[A] =
      F.flatMap(raw(uri, queries, headers, cm))(_.decode[A])
  }

  implicit def putBodyRequest[F[_], Bd, A](implicit encoder: EntityEncoder[F, Bd], 
                                                    decoder: EntityDecoder[F, A], 
                                                    F: MonadError[F, Throwable]) = new PutWithBodyRequest[Client[F], F, Bd, A] {
    type Resp = Response[F]

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Client[F]]): F[Resp] = {
      val requestF = Request[F](Method.PUT, Uri.unsafeFromString(deriveUriString(cm, uri)))
        .withQuery(queries)
        .withHeaders(headers)
        .withBody(body)

      F.flatMap(requestF)(_.run[A](cm))
    }

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Client[F]]): F[A] =
      F.flatMap(raw(uri, queries, headers, body, cm))(_.decode[A])
  }

  implicit def postRequest[F[_], A](implicit decoder: EntityDecoder[F, A], F: MonadError[F, Throwable]) = new PostRequest[Client[F], F, A] {
    type Resp = Response[F]

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Client[F]]): F[Resp] = {
      val request = Request[F](Method.POST, Uri.unsafeFromString(deriveUriString(cm, uri)))
        .withQuery(queries)
        .withHeaders(headers)

      request.run[A](cm)
    }

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Client[F]]): F[A] =
      F.flatMap(raw(uri, queries, headers, cm))(_.decode[A])
  }

  implicit def postBodyRequest[F[_], Bd, A](implicit encoder: EntityEncoder[F, Bd], 
                                                     decoder: EntityDecoder[F, A], 
                                                     F: MonadError[F, Throwable]) = new PostWithBodyRequest[Client[F], F, Bd, A] {
    type Resp = Response[F]

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Client[F]]): F[Resp] = {
      val requestF = Request[F](Method.POST, Uri.unsafeFromString(deriveUriString(cm, uri)))
        .withQuery(queries)
        .withHeaders(headers)
        .withBody(body)

      F.flatMap(requestF)(_.run[A](cm))
    }

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Client[F]]): F[A] =
      F.flatMap(raw(uri, queries, headers, body, cm))(_.decode[A])
  }

  implicit def deleteRequest[F[_], A](implicit decoder: EntityDecoder[F, A], F: MonadError[F, Throwable]) = new DeleteRequest[Client[F], F, A] {
    type Resp = Response[F]

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Client[F]]): F[Resp] = {
      val request = Request[F](Method.DELETE, Uri.unsafeFromString(deriveUriString(cm, uri)))
        .withQuery(queries)
        .withHeaders(headers)

      request.run[A](cm)
    }

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Client[F]]): F[A] =
      F.flatMap(raw(uri, queries, headers, cm))(_.decode[A])
  }
}
