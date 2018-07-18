package typedapi.shared

import shapeless._

import scala.annotation.implicitNotFound

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
sealed trait SegmentParam[K, V] extends ApiElement

/** Query parameter which represents its key as singleton type and describes the value type.
  * 
  * @param name query key
  */
sealed trait QueryParam[K, V] extends ApiElement

/** Header which represents its key as singleton type and describes the value type.
  * 
  * @param name header key
  */
sealed trait HeaderParam[K, V] extends ApiElement

/** Request body type description. */
final case class ReqBodyElement[MT <: MediaType, A]() extends ApiElement

trait MethodElement extends ApiElement
final case class GetElement[MT <: MediaType, A]() extends MethodElement
final case class PutElement[MT <: MediaType, A]() extends MethodElement
final case class PutWithBodyElement[BMT <: MediaType, Bd, MT <: MediaType, A]() extends MethodElement
final case class PostElement[MT <: MediaType, A]() extends MethodElement
final case class PostWithBodyElement[BMT <: MediaType, Bd, MT <: MediaType, A]() extends MethodElement
final case class DeleteElement[MT <: MediaType, A]() extends MethodElement

@implicitNotFound("""You try to add a request body to a method which doesn't expect one.

method: ${M}
""")
trait MethodToReqBody[M <: MethodElement, MT <: MediaType, Bd] {

  type Out <: MethodElement
}

trait MethodToReqBodyLowPrio {

  implicit def putToReqBody[MT <: MediaType, A, BMT <: MediaType, Bd] = new MethodToReqBody[PutElement[MT, A], BMT, Bd] {
    type Out = PutWithBodyElement[BMT, Bd, MT, A]
  }

  implicit def postToReqBody[MT <: MediaType, A, BMT <: MediaType, Bd] = new MethodToReqBody[PostElement[MT, A], BMT, Bd] {
    type Out = PostWithBodyElement[BMT, Bd, MT, A]
  }
}

trait MediaType {
  def value: String
}
case object `Application/Json` extends MediaType {
  val value = "application/json"
}
case object `Text/Plain` extends MediaType {
  val value = "text/plain"
}
