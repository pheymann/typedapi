package typedapi.shared

import shapeless._
import shapeless.labelled.FieldType

trait ApiOp

sealed trait FixedHeaders[H <: HList] extends ApiOp

sealed trait SegmentInput extends ApiOp
sealed trait QueryInput extends ApiOp
sealed trait HeaderInput extends ApiOp
sealed trait RawHeadersInput extends ApiOp

trait MethodType extends ApiOp
sealed trait GetCall extends MethodType
sealed trait PutCall extends MethodType
sealed trait PutWithBodyCall extends MethodType
sealed trait PostCall extends MethodType
sealed trait PostWithBodyCall extends MethodType
sealed trait DeleteCall extends MethodType

/** Separates uri, input description, method  and out type from `ApiList`. 
  * 
  *   Example:
  *     val api: FinalCons[Get[Json, Foo] :: Segment["name".type, String] :: "find".type :: HNil] = := :> "find" :> Segment[String]('name) :> Get[Foo]
  *     val trans: ("name".type :: SegmentInput :: HNil, 'name.type :: HNil, String :: HNil], Field[Json, GetCall], Foo)
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

  implicit def fixedHeaderElementTransformer[H <: HList, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[FixedHeadersElement[H], (El, KIn, VIn, M, Out), (FixedHeaders[H] :: El, KIn, VIn, M, Out)]

  implicit def rawHeadersElementTransformer[El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[RawHeadersParam.type, (El, KIn, VIn, M, Out), (RawHeadersInput :: El, RawHeadersField.T :: KIn, Map[String, String] :: VIn, M, Out)]

  implicit def getTransformer[MT <: MediaType, A] = at[GetElement[MT, A], Unit, (HNil, HNil, HNil, GetCall, FieldType[MT, A])]

  implicit def putTransformer[MT <: MediaType, A] = at[PutElement[MT, A], Unit, (HNil, HNil, HNil, PutCall, FieldType[MT, A])]

  implicit def putWithBodyTransformer[BMT <: MediaType, Bd, MT <: MediaType, A] = 
    at[PutWithBodyElement[BMT, Bd, MT, A], Unit, (HNil, FieldType[BMT, BodyField.T] :: HNil, Bd :: HNil, PutWithBodyCall, FieldType[MT, A])]

  implicit def postTransformer[MT <: MediaType, A] = at[PostElement[MT, A], Unit, (HNil, HNil, HNil, PostCall, FieldType[MT, A])]

  implicit def postWithBodyTransformer[BMT <: MediaType, Bd, MT <: MediaType, A] = 
    at[PostWithBodyElement[BMT, Bd, MT, A], Unit, (HNil, FieldType[BMT, BodyField.T] :: HNil, Bd :: HNil, PostWithBodyCall, FieldType[MT, A])]

  implicit def deleteTransformer[MT <: MediaType, A] = at[DeleteElement[MT, A], Unit, (HNil, HNil, HNil, DeleteCall, FieldType[MT, A])]
}
