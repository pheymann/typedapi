package typedapi.client

import shapeless._
import shapeless.labelled.FieldType

sealed trait ApiOp

sealed trait SegmentInput extends ApiOp
sealed trait QueryInput extends ApiOp
sealed trait HeaderInput extends ApiOp
sealed trait RawHeadersInput extends ApiOp

sealed trait GetCall[A] extends ApiOp
sealed trait PutCall[A] extends ApiOp
sealed trait PutWithBodyCall[Bd, A] extends ApiOp
sealed trait PostCall[A] extends ApiOp
sealed trait PostWithBodyCall[Bd, A] extends ApiOp
sealed trait DeleteCall[A] extends ApiOp

/** Separates uri and input description from `ApiList`. 
  *   Example:
  *     val api: FinalCons[Get[Foo] :: Segment["name".type, String] :: "find".type :: HNil] = := :> "find" :> Segment[String]('name) :> Get[Foo]
  *     val trans: (ElementTypes["name".type :: SegmentInput] :: HNil, InputTypes[FieldType['name.type, String] :: HNil])
  * We can use this to define our expected input.
  */
trait ApiTransformer {

  import TypeLevelFoldFunction.at

  implicit def pathElementTransformer[S, El <: HList, In <: HList] = at[Path[S], (El, In), (S :: El, In)]

  implicit def segmentElementTransformer[S <: Symbol, A, El <: HList, In <: HList] = at[SegmentParam[S, A], (El, In), (SegmentInput :: El, FieldType[S, A] :: In)]

  implicit def queryElementTransformer[S <: Symbol, A, El <: HList, In <: HList] = at[QueryParam[S, A], (El, In), (QueryInput :: El, FieldType[S, A] :: In)]

  implicit def queryListElementTransformer[S <: Symbol, A, El <: HList, In <: HList] = at[QueryParam[S, List[A]], (El, In), (QueryInput :: El, FieldType[S, List[A]] :: In)]

  implicit def headerElementTransformer[S <: Symbol, A, El <: HList, In <: HList] = at[HeaderParam[S, A], (El, In), (HeaderInput :: El, FieldType[S, A] :: In)]

  implicit def rawHeadersElementTransformer[El <: HList, In <: HList] = at[RawHeaders.type, (El, In), (RawHeadersInput :: El, FieldType[RawHeadersField.T, Map[String, String]] :: In)]

  implicit def getTransformer[A] = at[Get[A], (HNil, HNil), (GetCall[A] :: HNil, HNil)]

  implicit def putTransformer[A] = at[Put[A], (HNil, HNil), (PutCall[A] :: HNil, HNil)]

  implicit def putWithBodyTransformer[Bd, A] = at[PutWithBody[Bd, A], (HNil, HNil), (PutWithBodyCall[Bd, A] :: HNil, FieldType[BodyField.T, Bd] :: HNil)]

  implicit def postTransformer[A] = at[Post[A], (HNil, HNil), (PostCall[A] :: HNil, HNil)]

  implicit def postWithBodyTransformer[Bd, A] = at[PostWithBody[Bd, A], (HNil, HNil), (PostWithBodyCall[Bd, A] :: HNil, FieldType[BodyField.T, Bd] :: HNil)]

  implicit def deleteTransformer[A] = at[Delete[A], (HNil, HNil), (DeleteCall[A] :: HNil, HNil)]
}
