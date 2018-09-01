package typedapi.shared

import shapeless._
import shapeless.labelled.FieldType

import scala.annotation.implicitNotFound

trait ApiOp

sealed trait SegmentInput extends ApiOp
sealed trait QueryInput extends ApiOp
sealed trait HeaderInput extends ApiOp

sealed trait FixedHeader[K, V] extends ApiOp
sealed trait ClientHeader[K, V] extends ApiOp
sealed trait ClientHeaderInput extends ApiOp
sealed trait ClientHeaderCollInput extends ApiOp
sealed trait ServerHeaderSend[K, V] extends ApiOp
sealed trait ServerHeaderMatchInput extends ApiOp

trait MethodType extends ApiOp
sealed trait GetCall extends MethodType
sealed trait PutCall extends MethodType
sealed trait PutWithBodyCall extends MethodType
sealed trait PostCall extends MethodType
sealed trait PostWithBodyCall extends MethodType
sealed trait DeleteCall extends MethodType

/** Transforms a [[MethodType]] to a `String`. */
@implicitNotFound("Missing String transformation for this method = ${M}.")
trait MethodToString[M <: MethodType] {

  def show: String
}

trait MethodToStringLowPrio {

  implicit val getToStr      = new MethodToString[GetCall] { val show = "GET" }
  implicit val putToStr      = new MethodToString[PutCall] { val show = "PUT" }
  implicit val putBodyToStr  = new MethodToString[PutWithBodyCall] { val show = "PUT" }
  implicit val postToStr     = new MethodToString[PostCall] { val show = "POST" }
  implicit val postBodyToStr = new MethodToString[PostWithBodyCall] { val show = "POST" }
  implicit val deleteToStr   = new MethodToString[DeleteCall] { val show = "DELETE" }
}

/** Tranforms API type shape into five distinct types:
  *  - El:  elements of the API (path elements, segment/query/header input placeholder, etc.)
  *  - KIn: expected input key types (from parameters)
  *  - VIn: expected input value types (from parameters)
  *  - M:   method type 
  *  - Out: output type
  * 
  * ```
  * val api: TypeCarrier[Get[Json, Foo] :: Segment["name".type, String] :: "find".type :: HNil]
  * val trans: ("name".type :: SegmentInput :: HNil, "name".type :: HNil, String :: HNil], Field[Json, GetCall], Foo)
  * ```
  */
object ApiTransformer extends TplPoly2 {

  implicit def pathElementTransformer[S, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[PathElement[S], (El, KIn, VIn, M, Out), (S :: El, KIn, VIn, M, Out)]

  implicit def segmentParamTransformer[S, A, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[SegmentParam[S, A], (El, KIn, VIn, M, Out), (SegmentInput :: El, S :: KIn, A :: VIn, M, Out)]

  implicit def queryParamTransformer[S, A, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[QueryParam[S, A], (El, KIn, VIn, M, Out), (QueryInput :: El, S :: KIn, A :: VIn, M, Out)]

  implicit def queryListParamTransformer[S, A, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[QueryParam[S, List[A]], (El, KIn, VIn, M, Out), (QueryInput :: El, S :: KIn, List[A] :: VIn, M, Out)]

  implicit def headerParamTransformer[S, A, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[HeaderParam[S, A], (El, KIn, VIn, M, Out), (HeaderInput :: El, S :: KIn, A :: VIn, M, Out)]

  implicit def fixedHeaderElementTransformer[K, V, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[FixedHeaderElement[K, V], (El, KIn, VIn, M, Out), (FixedHeader[K, V] :: El, KIn, VIn, M, Out)]

  implicit def clientHeaderElementTransformer[K, V, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[ClientHeaderElement[K, V], (El, KIn, VIn, M, Out), (ClientHeader[K, V] :: El, KIn, VIn, M, Out)]

  implicit def clientHeaderParamTransformer[K, V, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[ClientHeaderParam[K, V], (El, KIn, VIn, M, Out), (ClientHeaderInput :: El, K :: KIn, V :: VIn, M, Out)]

  implicit def clientHeaderCollParamTransformer[V, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[ClientHeaderCollParam[V], (El, KIn, VIn, M, Out), (ClientHeaderCollInput :: El, KIn, Map[String, V] :: VIn, M, Out)]

  implicit def serverHeaderSendElementTransformer[K, V, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[ServerHeaderSendElement[K, V], (El, KIn, VIn, M, Out), (ServerHeaderSend[K, V] :: El, KIn, VIn, M, Out)]

  implicit def serverHeaderMatchParamTransformer[K, V, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[ServerHeaderMatchParam[K, V], (El, KIn, VIn, M, Out), (ServerHeaderMatchInput :: El, K :: KIn, Map[String, V] :: VIn, M, Out)]

  implicit def getTransformer[MT <: MediaType, A] = at[GetElement[MT, A], Unit, (HNil, HNil, HNil, GetCall, FieldType[MT, A])]

  implicit def putTransformer[MT <: MediaType, A] = at[PutElement[MT, A], Unit, (HNil, HNil, HNil, PutCall, FieldType[MT, A])]

  implicit def putWithBodyTransformer[BMT <: MediaType, Bd, MT <: MediaType, A] = 
    at[PutWithBodyElement[BMT, Bd, MT, A], Unit, (HNil, FieldType[BMT, BodyField.T] :: HNil, Bd :: HNil, PutWithBodyCall, FieldType[MT, A])]

  implicit def postTransformer[MT <: MediaType, A] = at[PostElement[MT, A], Unit, (HNil, HNil, HNil, PostCall, FieldType[MT, A])]

  implicit def postWithBodyTransformer[BMT <: MediaType, Bd, MT <: MediaType, A] = 
    at[PostWithBodyElement[BMT, Bd, MT, A], Unit, (HNil, FieldType[BMT, BodyField.T] :: HNil, Bd :: HNil, PostWithBodyCall, FieldType[MT, A])]

  implicit def deleteTransformer[MT <: MediaType, A] = at[DeleteElement[MT, A], Unit, (HNil, HNil, HNil, DeleteCall, FieldType[MT, A])]

  implicit def hlistTransformer[H <: HList, In <: HList, Out](implicit folder: TplLeftFolder.Aux[this.type, H, Unit, Out]) = at[H, In, Out :: In]
}
