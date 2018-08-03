package typedapi.client

import RequestDataBuilder.{Data, DataWithBody}
import typedapi.shared._
import shapeless._

import scala.language.higherKinds
import scala.annotation.implicitNotFound

/** Basic api request element. Provides a function to create an effect representing the actual request. */
@implicitNotFound("""Cannot find ApiRequest instance for {{M}}. Do you miss some implicit value e.g. encoders/decoders?

ouput: ${Out}
context: ${F}""")
trait ApiRequest[M <: MethodType, D <: HList, C, F[_], Out] {

  type Resp

  def raw(data: D, cm: ClientManager[C]): F[Resp]
  def apply(data: D, cm: ClientManager[C]): F[Out]
}

trait ApiWithoutBodyRequest[M <: MethodType, C, F[_], Out] extends ApiRequest[M, Data, C, F, Out] {

  def raw(data: Data, cm: ClientManager[C]): F[Resp] = {
    val (uri :: queries :: headers :: HNil): Data = data

    raw(uri, queries, headers, cm)
  }
  def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[Resp]

  def apply(data: Data, cm: ClientManager[C]): F[Out] = {
    val (uri :: queries :: headers :: HNil): Data = data

    apply(uri, queries, headers, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[Out]
}

trait ApiWithBodyRequest[M <: MethodType, C, F[_], Bd, Out] extends ApiRequest[M, DataWithBody[Bd], C, F, Out] {

  def raw(data: DataWithBody[Bd], cm: ClientManager[C]): F[Resp] = {
    val (uri :: queries :: headers :: body :: HNil): DataWithBody[Bd] = data

    raw(uri, queries, headers, body, cm)
  }
  def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[C]): F[Resp]

  def apply(data: DataWithBody[Bd], cm: ClientManager[C]): F[Out] = {
    val (uri :: queries :: headers :: body :: HNil): DataWithBody[Bd] = data

    apply(uri, queries, headers, body, cm)
  }
  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[C]): F[Out]
}

trait GetRequest[C, F[_], A] extends ApiWithoutBodyRequest[GetCall, C, F, A]
trait PutRequest[C, F[_], A] extends ApiWithoutBodyRequest[PutCall, C, F, A]
trait PutWithBodyRequest[C, F[_], Bd, A] extends ApiWithBodyRequest[PutWithBodyCall, C, F, Bd, A]
trait PostRequest[C, F[_], A] extends ApiWithoutBodyRequest[PostCall, C, F, A]
trait PostWithBodyRequest[C, F[_], Bd, A] extends ApiWithBodyRequest[PostWithBodyCall, C, F, Bd, A]
trait DeleteRequest[C, F[_], A] extends ApiWithoutBodyRequest[DeleteCall, C, F, A]
