package typedapi.shared

import shapeless._

sealed trait ApiOp

sealed trait SegmentInput extends ApiOp
sealed trait QueryInput extends ApiOp
sealed trait HeaderInput extends ApiOp
sealed trait RawHeadersInput extends ApiOp

sealed trait MethodType extends ApiOp
sealed trait GetCall extends MethodType
sealed trait PutCall extends MethodType
sealed trait PutWithBodyCall extends MethodType
sealed trait PostCall extends MethodType
sealed trait PostWithBodyCall extends MethodType
sealed trait DeleteCall extends MethodType

/** Separates uri, input description and out type from `ApiList`. 
  *   Example:
  *     val api: FinalCons[Get[Foo] :: Segment["name".type, String] :: "find".type :: HNil] = := :> "find" :> Segment[String]('name) :> Get[Foo]
  *     val trans: ("name".type :: SegmentInput :: HNil, FieldType['name.type, String :: HNil], Foo)
  */
trait ApiTransformer {

  import TypeLevelFoldFunction.at

  implicit def pathElementTransformer[S, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[PathElement[S], (El, KIn, VIn, M, Out), (S :: El, KIn, VIn, M, Out)]

  implicit def segmentElementTransformer[S <: Symbol, A, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[SegmentParam[S, A], (El, KIn, VIn, M, Out), (SegmentInput :: El, S :: KIn, A :: VIn, M, Out)]

  implicit def queryElementTransformer[S <: Symbol, A, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[QueryParam[S, A], (El, KIn, VIn, M, Out), (QueryInput :: El, S :: KIn, A :: VIn, M, Out)]

  implicit def queryListElementTransformer[S <: Symbol, A, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[QueryParam[S, List[A]], (El, KIn, VIn, M, Out), (QueryInput :: El, S :: KIn, List[A] :: VIn, M, Out)]

  implicit def headerElementTransformer[S <: Symbol, A, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[HeaderParam[S, A], (El, KIn, VIn, M, Out), (HeaderInput :: El, S :: KIn, A :: VIn, M, Out)]

  implicit def rawHeadersElementTransformer[El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, Out] = 
    at[RawHeadersParam.type, (El, KIn, VIn, M, Out), (RawHeadersInput :: El, RawHeadersField.T :: KIn, Map[String, String] :: VIn, M, Out)]

  implicit def getTransformer[A] = at[GetElement[A], Unit, (HNil, HNil, HNil, GetCall, A)]

  implicit def putTransformer[A] = at[PutElement[A], Unit, (HNil, HNil, HNil, PutCall, A)]

  implicit def putWithBodyTransformer[Bd, A] = at[PutWithBodyElement[Bd, A], Unit, (HNil, BodyField.T :: HNil, Bd :: HNil, PutWithBodyCall, A)]

  implicit def postTransformer[A] = at[PostElement[A], Unit, (HNil, HNil, HNil, PostCall, A)]

  implicit def postWithBodyTransformer[Bd, A] = at[PostWithBodyElement[Bd, A], Unit, (HNil, BodyField.T :: HNil, Bd :: HNil, PostWithBodyCall, A)]

  implicit def deleteTransformer[A] = at[DeleteElement[A], Unit, (HNil, HNil, HNil, DeleteCall, A)]
}
