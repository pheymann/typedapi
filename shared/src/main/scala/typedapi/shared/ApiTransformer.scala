package typedapi.shared

import shapeless._

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

  implicit def pathElementTransformer[S, El <: HList, KIn <: HList, VIn <: HList, Out] = 
    at[PathElement[S], (El, KIn, VIn, Out), (S :: El, KIn, VIn, Out)]

  implicit def segmentElementTransformer[S <: Symbol, A, El <: HList, KIn <: HList, VIn <: HList, Out] = 
    at[SegmentParam[S, A], (El, KIn, VIn, Out), (SegmentInput :: El, S :: KIn, A :: VIn, Out)]

  implicit def queryElementTransformer[S <: Symbol, A, El <: HList, KIn <: HList, VIn <: HList, Out] = 
    at[QueryParam[S, A], (El, KIn, VIn, Out), (QueryInput :: El, S :: KIn, A :: VIn, Out)]

  implicit def queryListElementTransformer[S <: Symbol, A, El <: HList, KIn <: HList, VIn <: HList, Out] = 
    at[QueryParam[S, List[A]], (El, KIn, VIn, Out), (QueryInput :: El, S :: KIn, List[A] :: VIn, Out)]

  implicit def headerElementTransformer[S <: Symbol, A, El <: HList, KIn <: HList, VIn <: HList, Out] = 
    at[HeaderParam[S, A], (El, KIn, VIn, Out), (HeaderInput :: El, S :: KIn, A :: VIn, Out)]

  implicit def rawHeadersElementTransformer[El <: HList, KIn <: HList, VIn <: HList, Out] = 
    at[RawHeadersParam.type, (El, KIn, VIn, Out), (RawHeadersInput :: El, RawHeadersField.T :: KIn, Map[String, String] :: VIn, Out)]

  implicit def getTransformer[A] = at[GetElement[A], (HNil, HNil, HNil), (GetCall :: HNil, HNil, HNil, A)]

  implicit def putTransformer[A] = at[PutElement[A], (HNil, HNil, HNil), (PutCall :: HNil, HNil, HNil, A)]

  implicit def putWithBodyTransformer[Bd, A] = at[PutWithBodyElement[Bd, A], (HNil, HNil, HNil), (PutWithBodyCall[Bd] :: HNil, BodyField.T :: HNil, Bd :: HNil, A)]

  implicit def postTransformer[A] = at[PostElement[A], (HNil, HNil, HNil), (PostCall :: HNil, HNil, HNil, A)]

  implicit def postWithBodyTransformer[Bd, A] = at[PostWithBodyElement[Bd, A], (HNil, HNil, HNil), (PostWithBodyCall[Bd] :: HNil, BodyField.T :: HNil, Bd :: HNil, A)]

  implicit def deleteTransformer[A] = at[DeleteElement[A], (HNil, HNil, HNil), (DeleteCall :: HNil, HNil, HNil, A)]
}
