package typedapi.client

import shapeless._

import scala.language.higherKinds
import scala.annotation.implicitNotFound

/** Basic api request structure. Expected input data and return-type are defined for each method. */
trait ApiRequest[D <: HList, C, F[_]] {

  type Out

  def apply(data: D, cm: ClientManager[C]): F[Out]
}

@implicitNotFound("Cannot find GetRequest instance for:\n - value: ${A}\n - context: ${F}")
trait GetRequest[C, F[_], A] extends ApiRequest[RequestData[GetCall[A], Data], C, F] {

  type Out = A

  def apply(data: RequestData[GetCall[A], Data], cm: ClientManager[C]): F[Out] = {
    val ((uri :: queries :: headers :: HNil) :: HNil): Data :: HNil = data

    apply(uri, queries, headers, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[Out]
}

@implicitNotFound("Cannot find PutRequest with Body instance for:\n - value: ${A}\n - context: ${F}")
trait PutRequest[C, F[_], A] extends ApiRequest[RequestData[PutCall[A], Data], C, F] {

  type Out = A

  def apply(data: RequestData[PutCall[A], Data], cm: ClientManager[C]): F[Out] = {
    val ((uri :: queries :: headers :: HNil) :: HNil): Data :: HNil = data

    apply(uri, queries, headers, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[Out]
}

@implicitNotFound("Cannot find PutRequest with Body instance for:\n - value: ${A}\n - context: ${F}")
trait PutWithBodyRequest[C, F[_], Bd, A] extends ApiRequest[RequestData[PutWithBodyCall[Bd, A], DataWithBody[Bd]], C, F] {

  type Out = A

  def apply(data: RequestData[PutWithBodyCall[Bd, A], DataWithBody[Bd]], cm: ClientManager[C]): F[Out] = {
    val ((uri :: queries :: headers :: body :: HNil) :: HNil): DataWithBody[Bd] :: HNil = data

    apply(uri, queries, headers, body, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[C]): F[Out]
}

@implicitNotFound("Cannot find PostRequest with Body instance for:\n - value: ${A}\n - context: ${F}")
trait PostRequest[C, F[_], A] extends ApiRequest[RequestData[PostCall[A], Data], C, F] {

  type Out = A

  def apply(data: RequestData[PostCall[A], Data], cm: ClientManager[C]): F[Out] = {
    val ((uri :: queries :: headers :: HNil) :: HNil): Data :: HNil = data

    apply(uri, queries, headers, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[Out]
}

@implicitNotFound("Cannot find PostRequest with Body instance for:\n - value: ${A}\n - context: ${F}")
trait PostWithBodyRequest[C, F[_], Bd, A] extends ApiRequest[RequestData[PostWithBodyCall[Bd, A], DataWithBody[Bd]], C, F] {

  type Out = A

  def apply(data: RequestData[PostWithBodyCall[Bd, A], DataWithBody[Bd]], cm: ClientManager[C]): F[Out] = {
    val ((uri :: queries :: headers :: body :: HNil) :: HNil): DataWithBody[Bd] :: HNil = data

    apply(uri, queries, headers, body, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[C]): F[Out]
}

@implicitNotFound("Cannot find DeleteRequest instance for:\n - value: ${A}\n - context: ${F}")
trait DeleteRequest[C, F[_], A] extends ApiRequest[RequestData[DeleteCall[A], Data], C, F] {

  type Out = A

  def apply(data: RequestData[DeleteCall[A], Data], cm: ClientManager[C]): F[Out] = {
    val ((uri :: queries :: headers :: HNil) :: HNil): Data :: HNil = data

    apply(uri, queries, headers, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[Out]
}
