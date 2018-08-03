package typedapi.client

import typedapi.util._
import scalaj.http._

package object scalajhttp {

  type Id[A]       = A
  type Blocking[A] = Either[Exception, A]

  private def reduceQueries(queries: Map[String, List[String]]): Map[String, String] = 
    queries.map { case (key, values) => key -> values.mkString(",") }(collection.breakOut)

  implicit def getRequest[A](implicit decoder: Decoder[Id, A]) = new GetRequest[Http.type, Blocking, A] {
    type Resp = HttpResponse[String]

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Blocking[Resp] = {
      val req = cm.client(deriveUriString(cm, uri)).params(reduceQueries(queries)).headers(headers).method("GET")

      Right(req.asString)
    }

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Blocking[A] =
      raw(uri, queries, headers, cm).right.flatMap(resp => decoder(resp.body))
  }

  implicit def putRequest[A](implicit decoder: Decoder[Id, A]) = new PutRequest[Http.type, Blocking, A] {
    type Resp = HttpResponse[String]
    
    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Blocking[Resp] = {
      val req = cm.client(deriveUriString(cm, uri)).params(reduceQueries(queries)).headers(headers).method("PUT")

      Right(req.asString)
    }

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Blocking[A] =
      raw(uri, queries, headers, cm).right.flatMap(resp => decoder(resp.body))
  }

  implicit def putBodyRequest[Bd, A](implicit encoder: Encoder[Id, Bd], decoder: Decoder[Id, A]) = new PutWithBodyRequest[Http.type, Blocking, Bd, A] {
    type Resp = HttpResponse[String]
    
    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Http.type]): Blocking[Resp] = {
      val req = cm.client(deriveUriString(cm, uri)).params(reduceQueries(queries)).headers(headers).put(encoder(body))

      Right(req.asString)
    }

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Http.type]): Blocking[A] =
      raw(uri, queries, headers, body, cm).right.flatMap(resp => decoder(resp.body))
  }

  implicit def postRequest[A](implicit decoder: Decoder[Id, A]) = new PostRequest[Http.type, Blocking, A] {
    type Resp = HttpResponse[String]
    
    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Blocking[Resp] = {
      val req = cm.client(deriveUriString(cm, uri)).params(reduceQueries(queries)).headers(headers).method("POST")

      Right(req.asString)
    }

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Blocking[A] =
      raw(uri, queries, headers, cm).right.flatMap(resp => decoder(resp.body))
  }

  implicit def postBodyRequest[Bd, A](implicit encoder: Encoder[Id, Bd], decoder: Decoder[Id, A]) = new PostWithBodyRequest[Http.type, Blocking, Bd, A] {
    type Resp = HttpResponse[String]
    
    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Http.type]): Blocking[Resp] = {
      val req = cm.client(deriveUriString(cm, uri)).params(reduceQueries(queries)).headers(headers).postData(encoder(body))

      Right(req.asString)
    }

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Http.type]): Blocking[A] =
      raw(uri, queries, headers, body, cm).right.flatMap(resp => decoder(resp.body))
  }

  implicit def deleteRequest[A](implicit decoder: Decoder[Id, A]) = new DeleteRequest[Http.type, Blocking, A] {
    type Resp = HttpResponse[String]

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Blocking[Resp] = {
      val req = cm.client(deriveUriString(cm, uri)).params(reduceQueries(queries)).headers(headers).method("DELETE")

      Right(req.asString)
    }

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Blocking[A] =
      raw(uri, queries, headers, cm).right.flatMap(resp => decoder(resp.body))
  }
}
