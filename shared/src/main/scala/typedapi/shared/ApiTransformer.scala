package typedapi.shared

import shapeless._
import shapeless.labelled.FieldType

sealed trait ApiOp

sealed trait SegmentInput extends ApiOp
sealed trait QueryInput extends ApiOp
sealed trait HeaderInput extends ApiOp
sealed trait RawHeadersInput extends ApiOp

sealed trait GetCall extends ApiOp
sealed trait PutCall extends ApiOp
sealed trait PutWithBodyCall[Bd] extends ApiOp
sealed trait PostCall extends ApiOp
sealed trait PostWithBodyCall[Bd] extends ApiOp
sealed trait DeleteCall extends ApiOp

/** Separates uri, input description and out type from `ApiList`. 
  *   Example:
  *     val api: FinalCons[Get[Foo] :: Segment["name".type, String] :: "find".type :: HNil] = := :> "find" :> Segment[String]('name) :> Get[Foo]
  *     val trans: ("name".type :: SegmentInput :: HNil, FieldType['name.type, String :: HNil], Foo)
  */
trait ApiTransformer {

  import TypeLevelFoldFunction.at

  implicit def pathElementTransformer[S, El <: HList, In <: HList, Out] = 
    at[PathElement[S], (El, In, Out), (S :: El, In, Out)]

  implicit def segmentElementTransformer[S <: Symbol, A, El <: HList, In <: HList, Out] = 
    at[SegmentParam[S, A], (El, In, Out), (SegmentInput :: El, FieldType[S, A] :: In, Out)]

  implicit def queryElementTransformer[S <: Symbol, A, El <: HList, In <: HList, Out] = 
    at[QueryParam[S, A], (El, In, Out), (QueryInput :: El, FieldType[S, A] :: In, Out)]

  implicit def queryListElementTransformer[S <: Symbol, A, El <: HList, In <: HList, Out] = 
    at[QueryParam[S, List[A]], (El, In, Out), (QueryInput :: El, FieldType[S, List[A]] :: In, Out)]

  implicit def headerElementTransformer[S <: Symbol, A, El <: HList, In <: HList, Out] = 
    at[HeaderParam[S, A], (El, In, Out), (HeaderInput :: El, FieldType[S, A] :: In, Out)]

  implicit def rawHeadersElementTransformer[El <: HList, In <: HList, Out] = 
    at[RawHeadersParam.type, (El, In, Out), (RawHeadersInput :: El, FieldType[RawHeadersField.T, Map[String, String]] :: In, Out)]

  implicit def getTransformer[A] = at[GetElement[A], (HNil, HNil), (GetCall :: HNil, HNil, A)]

  implicit def putTransformer[A] = at[PutElement[A], (HNil, HNil), (PutCall :: HNil, HNil, A)]

  implicit def putWithBodyTransformer[Bd, A] = at[PutWithBodyElement[Bd, A], (HNil, HNil), (PutWithBodyCall[Bd] :: HNil, FieldType[BodyField.T, Bd] :: HNil, A)]

  implicit def postTransformer[A] = at[PostElement[A], (HNil, HNil), (PostCall :: HNil, HNil, A)]

  implicit def postWithBodyTransformer[Bd, A] = at[PostWithBodyElement[Bd, A], (HNil, HNil), (PostWithBodyCall[Bd] :: HNil, FieldType[BodyField.T, Bd] :: HNil, A)]

  implicit def deleteTransformer[A] = at[DeleteElement[A], (HNil, HNil), (DeleteCall :: HNil, HNil, A)]
}
