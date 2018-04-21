package typedapi.client

import RequestDataBuilder.{RequestData, Data, DataWithBody}
import typedapi.shared._
import shapeless._

import scala.language.higherKinds
import scala.annotation.implicitNotFound

/** Basic api request structure. Expected input data and return-type are defined for each method. */
trait ApiRequest[D <: HList, C, F[_], Out] {

  def apply(data: D, cm: ClientManager[C]): F[Out]
}

@implicitNotFound("""Cannot find GetRequest instance. Do you miss some implicit value e.g. encoders/decoders?

value: ${A}
context: ${F}""")
trait GetRequest[C, F[_], A] extends ApiRequest[RequestData[GetCall, Data], C, F, A] {

  def apply(data: RequestData[GetCall, Data], cm: ClientManager[C]): F[A] = {
    val (uri :: queries :: headers :: HNil): Data = data

    apply(uri, queries, headers, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[A]
}

@implicitNotFound("""Cannot find PutRequest instance. Do you miss some implicit value e.g. encoders/decoders?

value: ${A}
context: ${F}""")
trait PutRequest[C, F[_], A] extends ApiRequest[RequestData[PutCall, Data], C, F, A] {

  def apply(data: RequestData[PutCall, Data], cm: ClientManager[C]): F[A] = {
    val (uri :: queries :: headers :: HNil): Data = data

    apply(uri, queries, headers, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[A]
}

@implicitNotFound("""Cannot find PutRequest with body instance. Do you miss some implicit value e.g. encoders/decoders?

value: ${A}
body: ${Bd}
context: ${F}""")
trait PutWithBodyRequest[C, F[_], Bd, A] extends ApiRequest[RequestData[PutWithBodyCall[Bd], DataWithBody[Bd]], C, F, A] {

  def apply(data: RequestData[PutWithBodyCall[Bd], DataWithBody[Bd]], cm: ClientManager[C]): F[A] = {
    val (uri :: queries :: headers :: body :: HNil): DataWithBody[Bd] = data

    apply(uri, queries, headers, body, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[C]): F[A]
}

@implicitNotFound("""Cannot find PostRequest instance. Do you miss some implicit value e.g. encoders/decoders?

value: ${A}
context: ${F}""")
trait PostRequest[C, F[_], A] extends ApiRequest[RequestData[PostCall, Data], C, F, A] {

  def apply(data: RequestData[PostCall, Data], cm: ClientManager[C]): F[A] = {
    val (uri :: queries :: headers :: HNil): Data = data

    apply(uri, queries, headers, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[A]
}

@implicitNotFound("""Cannot find PutRequest with body instance. Do you miss some implicit value e.g. encoders/decoders?

value: ${A}
body: ${Bd}
context: ${F}""")
trait PostWithBodyRequest[C, F[_], Bd, A] extends ApiRequest[RequestData[PostWithBodyCall[Bd], DataWithBody[Bd]], C, F, A] {

  def apply(data: RequestData[PostWithBodyCall[Bd], DataWithBody[Bd]], cm: ClientManager[C]): F[A] = {
    val (uri :: queries :: headers :: body :: HNil): DataWithBody[Bd] = data

    apply(uri, queries, headers, body, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[C]): F[A]
}

@implicitNotFound("""Cannot find DeleteRequest instance. Do you miss some implicit value e.g. encoders/decoders?

value: ${A}
context: ${F}""")
trait DeleteRequest[C, F[_], A] extends ApiRequest[RequestData[DeleteCall, Data], C, F, A] {

  def apply(data: RequestData[DeleteCall, Data], cm: ClientManager[C]): F[A] = {
    val (uri :: queries :: headers :: HNil): Data = data

    apply(uri, queries, headers, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[A]
}
