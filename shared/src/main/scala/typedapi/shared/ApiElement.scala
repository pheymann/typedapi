package typedapi.shared

import shapeless.Witness

sealed trait ApiElement

/** Static path element represented as singleton type.
  * 
  * @param wit singleton type of static path element
  */
final case class PathElement[S](wit: Witness.Lt[S]) extends ApiElement

/** Dynamically set segment within an URI which has a unique name to reference it on input.
  * 
  * @param name unique name reference
  */
final case class SegmentParam[S <: Symbol, A](name: Witness.Lt[S]) extends ApiElement

final class SegmentHelper[A] {

  def apply[S <: Symbol](wit: Witness.Lt[S]) = SegmentParam[S, A](wit)
}

/** Query parameter which represents its key as singleton type and describes the value type.
  * 
  * @param name query key
  */
final case class QueryParam[S <: Symbol, A](name: Witness.Lt[S]) extends ApiElement

final class QueryHelper[A] {

  def apply[S <: Symbol](wit: Witness.Lt[S]) = QueryParam[S, A](wit)
}

/** Header which represents its key as singleton type and describes the value type.
  * 
  * @param name header key
  */
final case class HeaderParam[S <: Symbol, A](name: Witness.Lt[S]) extends ApiElement

final class HeaderHelper[A] {

  def apply[S <: Symbol](wit: Witness.Lt[S]) = HeaderParam[S, A](wit)
}

/** Convenience class to add headers as `Map[String, String]` patch. This class cannot guarantee type safety.
  * 
  * @param headers
  */
case object RawHeadersParam extends ApiElement

/** Request body type description. */
final case class ReqBodyElement[A]() extends ApiElement

sealed trait MethodElement extends ApiElement
final case class GetElement[A]() extends MethodElement
final case class PutElement[A]() extends MethodElement
final case class PutWithBodyElement[Bd, A]() extends MethodElement
final case class PostElement[A]() extends MethodElement
final case class PostWithBodyElement[Bd, A]() extends MethodElement
final case class DeleteElement[A]() extends MethodElement
