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
        // always consume response to prevent memory leak
        response.toStrict(bodyConsumerTimeout)
      }

  def getRequest[A](bodyConsumerTimeout: FiniteDuration)(implicit decoder: FromEntityUnmarshaller[A],
                                                                  ec: ExecutionContext, 
                                                                  mat: Materializer) = new GetRequest[HttpExt, Future, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[HttpExt]): Future[A] = {
      val request = mkRequest(deriveUriString(cm, uri), queries, headers).copy(HttpMethods.GET)

      execRequest(cm.client, request, bodyConsumerTimeout).flatMap { response =>
        Unmarshal(response.entity).to[A]
      }
    }
  }

  def putRequest[A](bodyConsumerTimeout: FiniteDuration)(implicit decoder: FromEntityUnmarshaller[A],
                                                                  ec: ExecutionContext, 
                                                                  mat: Materializer) = new PutRequest[HttpExt, Future, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[HttpExt]): Future[A] = {
      val request = mkRequest(deriveUriString(cm, uri), queries, headers).copy(HttpMethods.PUT)

      execRequest(cm.client, request, bodyConsumerTimeout).flatMap { response =>
        Unmarshal(response.entity).to[A]
      }
    }
  }

  def putBodyRequest[Bd, A](bodyConsumerTimeout: FiniteDuration)(implicit encoder: ToEntityMarshaller[Bd],
                                                                          decoder: FromEntityUnmarshaller[A],
                                                                          ec: ExecutionContext, 
                                                                          mat: Materializer) = new PutWithBodyRequest[HttpExt, Future, Bd, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[HttpExt]): Future[A] = {
      Marshal(body).to[RequestEntity].flatMap { marshalledBody =>
        val request = mkRequest(deriveUriString(cm, uri), queries, headers).copy(HttpMethods.PUT, entity = marshalledBody)

        execRequest(cm.client, request, bodyConsumerTimeout).flatMap { response =>
          Unmarshal(response.entity).to[A]
        }
      }
    }
  }

  def postRequest[A](bodyConsumerTimeout: FiniteDuration)(implicit decoder: FromEntityUnmarshaller[A],
                                                                   ec: ExecutionContext, 
                                                                   mat: Materializer) = new PostRequest[HttpExt, Future, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[HttpExt]): Future[A] = {
      val request = mkRequest(deriveUriString(cm, uri), queries, headers).copy(HttpMethods.POST)

      execRequest(cm.client, request, bodyConsumerTimeout).flatMap { response =>
        Unmarshal(response.entity).to[A]
      }
    }
  }

  def postBodyRequest[Bd, A](bodyConsumerTimeout: FiniteDuration)(implicit encoder: ToEntityMarshaller[Bd],
                                                                           decoder: FromEntityUnmarshaller[A],
                                                                           ec: ExecutionContext, 
                                                                           mat: Materializer) = new PostWithBodyRequest[HttpExt, Future, Bd, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[HttpExt]): Future[A] = {
      Marshal(body).to[RequestEntity].flatMap { marshalledBody =>
        val request = mkRequest(deriveUriString(cm, uri), queries, headers).copy(HttpMethods.POST, entity = marshalledBody)

        execRequest(cm.client, request, bodyConsumerTimeout).flatMap { response =>
          Unmarshal(response.entity).to[A]
        }
      }
    }
  }

  def deleteRequest[A](bodyConsumerTimeout: FiniteDuration)(implicit decoder: FromEntityUnmarshaller[A],
                                                                     ec: ExecutionContext, 
                                                                     mat: Materializer) = new GetRequest[HttpExt, Future, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[HttpExt]): Future[A] = {
      val request = mkRequest(deriveUriString(cm, uri), queries, headers).copy(HttpMethods.DELETE)

      execRequest(cm.client, request, bodyConsumerTimeout).flatMap { response =>
        Unmarshal(response.entity).to[A]
      }
    }
  }
}
