package typedapi.client

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.RequestEntity
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest, HttpResponse, Uri }
import akka.http.scaladsl.unmarshalling.{Unmarshal, FromEntityUnmarshaller}
import akka.http.scaladsl.marshalling.{Marshal, ToEntityMarshaller}
import akka.stream.Materializer

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ ExecutionContext, Future }

package object akkahttp {

  private def mkRequest(uri: String, queries: Map[String, List[String]], headers: Map[String, String]): HttpRequest =
    HttpRequest(
        uri = Uri(uri).withQuery(Uri.Query(queries.mapValues(_.mkString(",")))),
        headers = headers.map { case (key, value) => RawHeader(key, value) }(collection.breakOut)
      )

  private def execRequest(client: HttpExt, request: HttpRequest, bodyConsumerTimeout: FiniteDuration)
                         (implicit ec: ExecutionContext, 
                                   mat: Materializer): Future[HttpResponse] =
    client.singleRequest(request)
      .flatMap { response =>
        response.toStrict(bodyConsumerTimeout)
      }

  def rawGetRequest(bodyConsumerTimeout: FiniteDuration)(implicit ec: ExecutionContext, 
                                                                  mat: Materializer) = new RawGetRequest[HttpExt, Future] {
    type Resp = HttpResponse

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[HttpExt]): Future[Resp] = {
      val request = mkRequest(deriveUriString(cm, uri), queries, headers).copy(HttpMethods.GET)

      execRequest(cm.client, request, bodyConsumerTimeout)
    }
  }

  implicit def rawGetRequestImpl(implicit bodyConsumerTimeout: FiniteDuration,
                                          ec: ExecutionContext,
                                          mat: Materializer): RawGetRequest[HttpExt, Future] = rawGetRequest(bodyConsumerTimeout)

  def getRequest[A](bodyConsumerTimeout: FiniteDuration)(implicit decoder: FromEntityUnmarshaller[A],
                                                                  ec: ExecutionContext, 
                                                                  mat: Materializer) = new GetRequest[HttpExt, Future, A] {
    private val raw = rawGetRequest(bodyConsumerTimeout)

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[HttpExt]): Future[A] =
      raw(uri, queries, headers, cm).flatMap { response =>
        Unmarshal(response.entity).to[A]
      }
  }

  implicit def getRequestImpl[A](implicit bodyConsumerTimeout: FiniteDuration,
                                          decoder: FromEntityUnmarshaller[A],
                                          ec: ExecutionContext,
                                          mat: Materializer): GetRequest[HttpExt, Future, A] = getRequest(bodyConsumerTimeout)

  def rawPutRequest(bodyConsumerTimeout: FiniteDuration)(implicit ec: ExecutionContext, 
                                                                  mat: Materializer) = new RawPutRequest[HttpExt, Future] {
    type Resp = HttpResponse

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[HttpExt]): Future[Resp] = {
      val request = mkRequest(deriveUriString(cm, uri), queries, headers).copy(HttpMethods.PUT)

      execRequest(cm.client, request, bodyConsumerTimeout)
    }
  }

  implicit def rawPutRequestImpl(implicit bodyConsumerTimeout: FiniteDuration,
                                          ec: ExecutionContext,
                                          mat: Materializer): RawPutRequest[HttpExt, Future] = rawPutRequest(bodyConsumerTimeout)

  def putRequest[A](bodyConsumerTimeout: FiniteDuration)(implicit decoder: FromEntityUnmarshaller[A],
                                                                  ec: ExecutionContext, 
                                                                  mat: Materializer) = new PutRequest[HttpExt, Future, A] {
    private val raw = rawPutRequest(bodyConsumerTimeout)

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[HttpExt]): Future[A] =
      raw(uri, queries, headers, cm).flatMap { response =>
        Unmarshal(response.entity).to[A]
      }
  }

  implicit def putRequestImpl[A](implicit bodyConsumerTimeout: FiniteDuration,
                                          decoder: FromEntityUnmarshaller[A],
                                          ec: ExecutionContext,
                                          mat: Materializer): PutRequest[HttpExt, Future, A] = putRequest(bodyConsumerTimeout)

  def rawPutBodyRequest[Bd](bodyConsumerTimeout: FiniteDuration)(implicit encoder: ToEntityMarshaller[Bd],
                                                                          ec: ExecutionContext, 
                                                                          mat: Materializer) = new RawPutWithBodyRequest[HttpExt, Future, Bd] {
    type Resp = HttpResponse

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[HttpExt]): Future[Resp] = {
      Marshal(body).to[RequestEntity].flatMap { marshalledBody =>
        val request = mkRequest(deriveUriString(cm, uri), queries, headers - "Content-Type").copy(HttpMethods.PUT, entity = marshalledBody)

        execRequest(cm.client, request, bodyConsumerTimeout)
      }
    }
  }

  implicit def rawPutBodyRequestImpl[Bd](implicit bodyConsumerTimeout: FiniteDuration,
                                                  encoder: ToEntityMarshaller[Bd],
                                                  ec: ExecutionContext,
                                                  mat: Materializer): RawPutWithBodyRequest[HttpExt, Future, Bd] = rawPutBodyRequest(bodyConsumerTimeout)

  def putBodyRequest[Bd, A](bodyConsumerTimeout: FiniteDuration)(implicit encoder: ToEntityMarshaller[Bd],
                                                                          decoder: FromEntityUnmarshaller[A],
                                                                          ec: ExecutionContext, 
                                                                          mat: Materializer) = new PutWithBodyRequest[HttpExt, Future, Bd, A] {
    private val raw = rawPutBodyRequest(bodyConsumerTimeout)

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[HttpExt]): Future[A] =
      raw(uri, queries, headers, body, cm).flatMap { response =>
        Unmarshal(response.entity).to[A]
      }
  }

  implicit def putBodyRequestImpl[Bd, A](implicit bodyConsumerTimeout: FiniteDuration,
                                                  encoder: ToEntityMarshaller[Bd],
                                                  decoder: FromEntityUnmarshaller[A],
                                                  ec: ExecutionContext,
                                                  mat: Materializer): PutWithBodyRequest[HttpExt, Future, Bd, A] = putBodyRequest(bodyConsumerTimeout)

  def rawPostRequest(bodyConsumerTimeout: FiniteDuration)(implicit ec: ExecutionContext, 
                                                                   mat: Materializer) = new RawPostRequest[HttpExt, Future] {
    type Resp = HttpResponse

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[HttpExt]): Future[Resp] = {
      val request = mkRequest(deriveUriString(cm, uri), queries, headers).copy(HttpMethods.POST)

      execRequest(cm.client, request, bodyConsumerTimeout)
    }
  }

  implicit def rawPostRequestImpl(implicit bodyConsumerTimeout: FiniteDuration,
                                           ec: ExecutionContext,
                                           mat: Materializer): RawPostRequest[HttpExt, Future] = rawPostRequest(bodyConsumerTimeout)

  def postRequest[A](bodyConsumerTimeout: FiniteDuration)(implicit decoder: FromEntityUnmarshaller[A],
                                                                   ec: ExecutionContext, 
                                                                   mat: Materializer) = new PostRequest[HttpExt, Future, A] {
    private val raw = rawPostRequest(bodyConsumerTimeout)

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[HttpExt]): Future[A] =
      raw(uri, queries, headers, cm).flatMap { response =>
        Unmarshal(response.entity).to[A]
      }
  }

  implicit def postRequestImpl[A](implicit bodyConsumerTimeout: FiniteDuration,
                                           decoder: FromEntityUnmarshaller[A],
                                           ec: ExecutionContext,
                                           mat: Materializer): PostRequest[HttpExt, Future, A] = postRequest(bodyConsumerTimeout)

  def rawPostBodyRequest[Bd](bodyConsumerTimeout: FiniteDuration)(implicit encoder: ToEntityMarshaller[Bd],
                                                                           ec: ExecutionContext, 
                                                                           mat: Materializer) = new RawPostWithBodyRequest[HttpExt, Future, Bd] {
    type Resp = HttpResponse

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[HttpExt]): Future[Resp] = {
      Marshal(body).to[RequestEntity].flatMap { marshalledBody =>
        val request = mkRequest(deriveUriString(cm, uri), queries, headers - "Content-Type").copy(HttpMethods.POST, entity = marshalledBody)

        execRequest(cm.client, request, bodyConsumerTimeout)
      }
    }
  }

  implicit def rawPostBodyRequestImpl[Bd](implicit bodyConsumerTimeout: FiniteDuration,
                                                   encoder: ToEntityMarshaller[Bd],
                                                   ec: ExecutionContext,
                                                   mat: Materializer): RawPostWithBodyRequest[HttpExt, Future, Bd] = rawPostBodyRequest(bodyConsumerTimeout)

  def postBodyRequest[Bd, A](bodyConsumerTimeout: FiniteDuration)(implicit encoder: ToEntityMarshaller[Bd],
                                                                           decoder: FromEntityUnmarshaller[A],
                                                                           ec: ExecutionContext, 
                                                                           mat: Materializer) = new PostWithBodyRequest[HttpExt, Future, Bd, A] {
    private val raw = rawPostBodyRequest(bodyConsumerTimeout)

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[HttpExt]): Future[A] =
      raw(uri, queries, headers, body, cm).flatMap { response =>
        Unmarshal(response.entity).to[A]
      }
  }

  implicit def postBodyRequestImpl[Bd, A](implicit bodyConsumerTimeout: FiniteDuration,
                                                   encoder: ToEntityMarshaller[Bd],
                                                   decoder: FromEntityUnmarshaller[A],
                                                   ec: ExecutionContext,
                                                   mat: Materializer): PostWithBodyRequest[HttpExt, Future, Bd, A] = postBodyRequest(bodyConsumerTimeout)

  def rawDeleteRequest(bodyConsumerTimeout: FiniteDuration)(implicit ec: ExecutionContext, 
                                                                     mat: Materializer) = new RawDeleteRequest[HttpExt, Future] {
    type Resp = HttpResponse

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[HttpExt]): Future[Resp] = {
      val request = mkRequest(deriveUriString(cm, uri), queries, headers).copy(HttpMethods.DELETE)

      execRequest(cm.client, request, bodyConsumerTimeout)
    }
  }

  implicit def rawDeleteRequestImpl(implicit bodyConsumerTimeout: FiniteDuration,
                                             ec: ExecutionContext,
                                             mat: Materializer): RawDeleteRequest[HttpExt, Future] = rawDeleteRequest(bodyConsumerTimeout)

  def deleteRequest[A](bodyConsumerTimeout: FiniteDuration)(implicit decoder: FromEntityUnmarshaller[A],
                                                                     ec: ExecutionContext, 
                                                                     mat: Materializer) = new DeleteRequest[HttpExt, Future, A] {
    private val raw = rawDeleteRequest(bodyConsumerTimeout)

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[HttpExt]): Future[A] =
      raw(uri, queries, headers, cm).flatMap { response =>
        Unmarshal(response.entity).to[A]
      }
  }

  implicit def deleteRequestImpl[A](implicit bodyConsumerTimeout: FiniteDuration,
                                             decoder: FromEntityUnmarshaller[A],
                                             ec: ExecutionContext,
                                             mat: Materializer): DeleteRequest[HttpExt, Future, A] = deleteRequest(bodyConsumerTimeout)
}
