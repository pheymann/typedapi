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

final class ElementTypes[H <: HList]
final class InputTypes[H <: HList]

/** Separates uri and input description from `ApiList`. 
  *   Example:
  *     val api: FinalCons[Get[Foo] :: Segment["name".type, String] :: "find".type :: HNil] = := :> "find" :> Segment[String]('name) :> Get[Foo]
  *     val trans: (ElementTypes["name".type :: SegmentInput] :: HNil, InputTypes[FieldType['name.type, String] :: HNil])
  * We can use this to define our expected input.
  */
trait ApiTransformer {

  import TypeLevelFoldFunction.at

  private def elements[H <: HList] = new ElementTypes[H]
  private def inputs[H <: HList]   = new InputTypes[H]

  implicit def pathElementTransformer[S, El <: HList, In <: HList] = at[Path[S], (ElementTypes[El], InputTypes[In])] { case (_, inputs) => 
    (elements[S :: El], inputs)
  }

  implicit def segmentElementTransformer[S <: Symbol, A, El <: HList, In <: HList] = at[SegmentParam[S, A], (ElementTypes[El], InputTypes[In])] { _ =>
    (elements[SegmentInput :: El], inputs[FieldType[S, A] :: In])
  }

  implicit def queryElementTransformer[S <: Symbol, A, El <: HList, In <: HList] = at[QueryParam[S, A], (ElementTypes[El], InputTypes[In])] { _ =>
    (elements[QueryInput :: El], inputs[FieldType[S, A] :: In])
  }

  implicit def queryListElementTransformer[S <: Symbol, A, El <: HList, In <: HList] = at[QueryParam[S, List[A]], (ElementTypes[El], InputTypes[In])] { _ =>
    (elements[QueryInput :: El], inputs[FieldType[S, List[A]] :: In])
  }

  implicit def headerElementTransformer[S <: Symbol, A, El <: HList, In <: HList] = at[HeaderParam[S, A], (ElementTypes[El], InputTypes[In])] { _ =>
    (elements[HeaderInput :: El], inputs[FieldType[S, A] :: In])
  }

  implicit def rawHeadersElementTransformer[El <: HList, In <: HList] = at[RawHeaders.type, (ElementTypes[El], InputTypes[In])] { _ =>
    (elements[RawHeadersInput :: El], inputs[FieldType[RawHeadersField.T, Map[String, String]] :: In])
  }

  implicit def getTransformer[A] = at[Get[A], (ElementTypes[HNil], InputTypes[HNil])] { case (_, inputs) => 
    (elements[GetCall[A] :: HNil], inputs) 
  }

  implicit def putTransformer[A] = at[Put[A], (ElementTypes[HNil], InputTypes[HNil])] { case (_, inputs) => 
    (elements[PutCall[A] :: HNil], inputs) 
  }

  implicit def putWithBodyTransformer[Bd, A] = at[PutWithBody[Bd, A], (ElementTypes[HNil], InputTypes[HNil])] { case (_, _) => 
    (elements[PutWithBodyCall[Bd, A] :: HNil], inputs[FieldType[BodyField.T, Bd] :: HNil])
  }

  implicit def postTransformer[A] = at[Post[A], (ElementTypes[HNil], InputTypes[HNil])] { case (_, inputs) => 
    (elements[PostCall[A] :: HNil], inputs) 
  }

  implicit def postWithBodyTransformer[Bd, A] = at[PostWithBody[Bd, A], (ElementTypes[HNil], InputTypes[HNil])] { case (_, _) => 
    (elements[PostWithBodyCall[Bd, A] :: HNil], inputs[FieldType[BodyField.T, Bd] :: HNil])
  }

  implicit def deleteTransformer[A] = at[Delete[A], (ElementTypes[HNil], InputTypes[HNil])] { case (_, inputs) => 
    (elements[DeleteCall[A] :: HNil], inputs) 
  }
}
