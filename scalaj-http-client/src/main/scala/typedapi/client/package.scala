package typedapi.client

import typedapi.util._
import scalaj.http._

package object scalajhttp {

  type Id[A]       = A
  type Blocking[A] = Either[Exception, A]

  private def reduceQueries(queries: Map[String, List[String]]): Map[String, String] = 
    queries.map { case (key, values) => key -> values.mkString(",") }(collection.breakOut)

  implicit def rawGetRequest = new RawGetRequest[Http.type, Id] {
    type Resp = HttpResponse[String]

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Id[Resp] = {
      val req = cm.client(deriveUriString(cm, uri)).params(reduceQueries(queries)).headers(headers).method("GET")

      req.asString
    }
  }

  implicit def getRequest[A](implicit decoder: Decoder[Id, A]) = new GetRequest[Http.type, Blocking, A] {
    private val raw = rawGetRequest

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Blocking[A] =
      decoder(raw(uri, queries, headers, cm).body)
  }

  implicit def rawPutRequest = new RawPutRequest[Http.type, Id] {
    type Resp = HttpResponse[String]
    
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Id[Resp] = {
      val req = cm.client(deriveUriString(cm, uri)).params(reduceQueries(queries)).headers(headers).method("PUT")

      req.asString
    }
  }

  implicit def putRequest[A](implicit decoder: Decoder[Id, A]) = new PutRequest[Http.type, Blocking, A] {
    private val raw = rawPutRequest

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Blocking[A] =
      decoder(raw(uri, queries, headers, cm).body)
  }

  implicit def rawPutBodyRequest[Bd](implicit encoder: Encoder[Id, Bd]) = new RawPutWithBodyRequest[Http.type, Id, Bd] {
    type Resp = HttpResponse[String]
    
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Http.type]): Id[Resp] = {
      val req = cm.client(deriveUriString(cm, uri)).params(reduceQueries(queries)).headers(headers).put(encoder(body))

      req.asString
    }
  }

  implicit def putBodyRequest[Bd, A](implicit encoder: Encoder[Id, Bd], decoder: Decoder[Id, A]) = new PutWithBodyRequest[Http.type, Blocking, Bd, A] {
    private val raw = rawPutBodyRequest[Bd]

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Http.type]): Blocking[A] =
      decoder(raw(uri, queries, headers, body, cm).body)
  }

  implicit def rawPostRequest = new RawPostRequest[Http.type, Id] {
    type Resp = HttpResponse[String]
    
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Id[Resp] = {
      val req = cm.client(deriveUriString(cm, uri)).params(reduceQueries(queries)).headers(headers).method("POST")

      req.asString
    }
  }

  implicit def postRequest[A](implicit decoder: Decoder[Id, A]) = new PostRequest[Http.type, Blocking, A] {
    private val raw = rawPostRequest

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Blocking[A] =
      decoder(raw(uri, queries, headers, cm).body)
  }

  implicit def rawPostBodyRequest[Bd](implicit encoder: Encoder[Id, Bd]) = new RawPostWithBodyRequest[Http.type, Id, Bd] {
    type Resp = HttpResponse[String]
    
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Http.type]): Id[Resp] = {
      val req = cm.client(deriveUriString(cm, uri)).params(reduceQueries(queries)).headers(headers).postData(encoder(body))

      req.asString
    }
  }

  implicit def postBodyRequest[Bd, A](implicit encoder: Encoder[Id, Bd], decoder: Decoder[Id, A]) = new PostWithBodyRequest[Http.type, Blocking, Bd, A] {
    private val raw = rawPostBodyRequest[Bd]

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[Http.type]): Blocking[A] =
      decoder(raw(uri, queries, headers, body, cm).body)
  }

  implicit def rawDeleteRequest = new RawDeleteRequest[Http.type, Id] {
    type Resp = HttpResponse[String]

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Id[Resp] = {
      val req = cm.client(deriveUriString(cm, uri)).params(reduceQueries(queries)).headers(headers).method("DELETE")

      req.asString
    }
  }

  implicit def deleteRequest[A](implicit decoder: Decoder[Id, A]) = new DeleteRequest[Http.type, Blocking, A] {
    private val raw = rawDeleteRequest

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[Http.type]): Blocking[A] =
      decoder(raw(uri, queries, headers, cm).body)
  }
}
