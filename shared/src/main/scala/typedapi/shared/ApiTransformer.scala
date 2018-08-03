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
sealed trait ServerHeader[K, V] extends ApiOp

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
trait ApiTransformer {

  import TypeLevelFoldFunction.at

  implicit def pathElementTransformer[S, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[PathElement[S], (El, KIn, VIn, M, Out), (S :: El, KIn, VIn, M, Out)]

  implicit def segmentElementTransformer[S, A, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[SegmentParam[S, A], (El, KIn, VIn, M, Out), (SegmentInput :: El, S :: KIn, A :: VIn, M, Out)]

  implicit def queryElementTransformer[S, A, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[QueryParam[S, A], (El, KIn, VIn, M, Out), (QueryInput :: El, S :: KIn, A :: VIn, M, Out)]

  implicit def queryListElementTransformer[S, A, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[QueryParam[S, List[A]], (El, KIn, VIn, M, Out), (QueryInput :: El, S :: KIn, List[A] :: VIn, M, Out)]

  implicit def headerElementTransformer[S, A, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[HeaderParam[S, A], (El, KIn, VIn, M, Out), (HeaderInput :: El, S :: KIn, A :: VIn, M, Out)]

  implicit def fixedHeaderElementTransformer[K, V, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[FixedHeaderElement[K, V], (El, KIn, VIn, M, Out), (FixedHeader[K, V] :: El, KIn, VIn, M, Out)]

  implicit def clientHeaderElementTransformer[K, V, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[ClientHeaderElement[K, V], (El, KIn, VIn, M, Out), (ClientHeader[K, V] :: El, KIn, VIn, M, Out)]

  implicit def serverHeaderElementTransformer[K, V, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[ServerHeaderElement[K, V], (El, KIn, VIn, M, Out), (ServerHeader[K, V] :: El, KIn, VIn, M, Out)]

  implicit def getTransformer[MT <: MediaType, A] = at[GetElement[MT, A], Unit, (HNil, HNil, HNil, GetCall, FieldType[MT, A])]

  implicit def putTransformer[MT <: MediaType, A] = at[PutElement[MT, A], Unit, (HNil, HNil, HNil, PutCall, FieldType[MT, A])]

  implicit def putWithBodyTransformer[BMT <: MediaType, Bd, MT <: MediaType, A] = 
    at[PutWithBodyElement[BMT, Bd, MT, A], Unit, (HNil, FieldType[BMT, BodyField.T] :: HNil, Bd :: HNil, PutWithBodyCall, FieldType[MT, A])]

  implicit def postTransformer[MT <: MediaType, A] = at[PostElement[MT, A], Unit, (HNil, HNil, HNil, PostCall, FieldType[MT, A])]

  implicit def postWithBodyTransformer[BMT <: MediaType, Bd, MT <: MediaType, A] = 
    at[PostWithBodyElement[BMT, Bd, MT, A], Unit, (HNil, FieldType[BMT, BodyField.T] :: HNil, Bd :: HNil, PostWithBodyCall, FieldType[MT, A])]

  implicit def deleteTransformer[MT <: MediaType, A] = at[DeleteElement[MT, A], Unit, (HNil, HNil, HNil, DeleteCall, FieldType[MT, A])]
}
