package typedapi.client

import RequestDataBuilder.{Data, DataWithBody}
import typedapi.shared._
import shapeless._

import scala.language.higherKinds
import scala.annotation.implicitNotFound

/** Basic api request element. Provides a function to create an effect representing the actual request. */
@implicitNotFound("""Cannot find RawApiRequest instance for ${M}.

context: ${F}""")
trait RawApiRequest[M <: MethodType, D <: HList, C, F[_]] {

  type Resp

  def apply(data: D, cm: ClientManager[C]): F[Resp]
}

@implicitNotFound("""Cannot find ApiRequest instance for ${M}. Do you miss some implicit value e.g. encoders/decoders?

ouput: ${Out}
context: ${F}""")
trait ApiRequest[M <: MethodType, D <: HList, C, F[_], Out] {

  def apply(data: D, cm: ClientManager[C]): F[Out]
}

trait RawApiWithoutBodyRequest[M <: MethodType, C, F[_]] extends RawApiRequest[M, Data, C, F] {

  def apply(data: Data, cm: ClientManager[C]): F[Resp] = {
    val (uri :: queries :: headers :: HNil): Data = data

    apply(uri, queries, headers, cm)
  }

  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[Resp]
}

trait ApiWithoutBodyRequest[M <: MethodType, C, F[_], Out] extends ApiRequest[M, Data, C, F, Out] {

  def apply(data: Data, cm: ClientManager[C]): F[Out] = {
    val (uri :: queries :: headers :: HNil): Data = data

    apply(uri, queries, headers, cm)
  }

  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: ClientManager[C]): F[Out]
}

trait RawApiWithBodyRequest[M <: MethodType, C, F[_], Bd] extends RawApiRequest[M, DataWithBody[Bd], C, F] {

  def apply(data: DataWithBody[Bd], cm: ClientManager[C]): F[Resp] = {
    val (uri :: queries :: headers :: body :: HNil): DataWithBody[Bd] = data

    apply(uri, queries, headers, body, cm)
  }

  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[C]): F[Resp]
}

trait ApiWithBodyRequest[M <: MethodType, C, F[_], Bd, Out] extends ApiRequest[M, DataWithBody[Bd], C, F, Out] {

  def apply(data: DataWithBody[Bd], cm: ClientManager[C]): F[Out] = {
    val (uri :: queries :: headers :: body :: HNil): DataWithBody[Bd] = data

    apply(uri, queries, headers, body, cm)
  }

  def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: ClientManager[C]): F[Out]
}

trait RawGetRequest[C, F[_]] extends RawApiWithoutBodyRequest[GetCall, C, F]
trait GetRequest[C, F[_], A] extends ApiWithoutBodyRequest[GetCall, C, F, A]
trait RawPutRequest[C, F[_]] extends RawApiWithoutBodyRequest[PutCall, C, F]
trait PutRequest[C, F[_], A] extends ApiWithoutBodyRequest[PutCall, C, F, A]
trait RawPutWithBodyRequest[C, F[_], Bd] extends RawApiWithBodyRequest[PutWithBodyCall, C, F, Bd]
trait PutWithBodyRequest[C, F[_], Bd, A] extends ApiWithBodyRequest[PutWithBodyCall, C, F, Bd, A]
trait RawPostRequest[C, F[_]] extends RawApiWithoutBodyRequest[PostCall, C, F]
trait PostRequest[C, F[_], A] extends ApiWithoutBodyRequest[PostCall, C, F, A]
trait RawPostWithBodyRequest[C, F[_], Bd] extends RawApiWithBodyRequest[PostWithBodyCall, C, F, Bd]
trait PostWithBodyRequest[C, F[_], Bd, A] extends ApiWithBodyRequest[PostWithBodyCall, C, F, Bd, A]
trait RawDeleteRequest[C, F[_]] extends RawApiWithoutBodyRequest[DeleteCall, C, F]
trait DeleteRequest[C, F[_], A] extends ApiWithoutBodyRequest[DeleteCall, C, F, A]
