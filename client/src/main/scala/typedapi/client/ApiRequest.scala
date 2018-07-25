package typedapi.client

import RequestDataBuilder.{Data, DataWithBody}
import typedapi.shared._
import shapeless._

import scala.language.higherKinds
import scala.annotation.implicitNotFound

/** Basic api request element. Provides a function to create an IO effect representing the actual request. */
trait ApiRequest[M <: MethodType, D <: HList, C, F[_], Out] {

  def apply(data: D, cm: ClientManager[C]): F[Out]
}

@implicitNotFound("""Cannot find GetRequest instance. Do you miss some implicit value e.g. encoders/decoders?

value: ${A}
context: ${F}""")
trait GetRequest[C, F[_], A] extends ApiRequest[GetCall, Data, C, F, A] {

  def apply(data: Data, cm: ClientManager[C]): F[A] = {
    val (uri :: queries :: headers :: HNil): Data = data

    apply(uri, queries, headers, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[A]
}

@implicitNotFound("""Cannot find PutRequest instance. Do you miss some implicit value e.g. encoders/decoders?

value: ${A}
context: ${F}""")
trait PutRequest[C, F[_], A] extends ApiRequest[PutCall, Data, C, F, A] {

  def apply(data: Data, cm: ClientManager[C]): F[A] = {
    val (uri :: queries :: headers :: HNil): Data = data

    apply(uri, queries, headers, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[A]
}

@implicitNotFound("""Cannot find PutRequest with body instance. Do you miss some implicit value e.g. encoders/decoders?

value: ${A}
body: ${Bd}
context: ${F}""")
trait PutWithBodyRequest[C, F[_], Bd, A] extends ApiRequest[PutWithBodyCall, DataWithBody[Bd], C, F, A] {

  def apply(data: DataWithBody[Bd], cm: ClientManager[C]): F[A] = {
    val (uri :: queries :: headers :: body :: HNil): DataWithBody[Bd] = data

    apply(uri, queries, headers, body, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[C]): F[A]
}

@implicitNotFound("""Cannot find PostRequest instance. Do you miss some implicit value e.g. encoders/decoders?

value: ${A}
context: ${F}""")
trait PostRequest[C, F[_], A] extends ApiRequest[PostCall, Data, C, F, A] {

  def apply(data: Data, cm: ClientManager[C]): F[A] = {
    val (uri :: queries :: headers :: HNil): Data = data

    apply(uri, queries, headers, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[A]
}

@implicitNotFound("""Cannot find PutRequest with body instance. Do you miss some implicit value e.g. encoders/decoders?

value: ${A}
body: ${Bd}
context: ${F}""")
trait PostWithBodyRequest[C, F[_], Bd, A] extends ApiRequest[PostWithBodyCall, DataWithBody[Bd], C, F, A] {

  def apply(data: DataWithBody[Bd], cm: ClientManager[C]): F[A] = {
    val (uri :: queries :: headers :: body :: HNil): DataWithBody[Bd] = data

    apply(uri, queries, headers, body, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[C]): F[A]
}

@implicitNotFound("""Cannot find DeleteRequest instance. Do you miss some implicit value e.g. encoders/decoders?

value: ${A}
context: ${F}""")
trait DeleteRequest[C, F[_], A] extends ApiRequest[DeleteCall, Data, C, F, A] {

  def apply(data: Data, cm: ClientManager[C]): F[A] = {
    val (uri :: queries :: headers :: HNil): Data = data

    apply(uri, queries, headers, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[A]
}
