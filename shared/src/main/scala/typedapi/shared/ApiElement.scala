package typedapi.shared

import scala.annotation.implicitNotFound

sealed trait ApiElement

/** Static path element represented as singleton type.
  * 
  * @param wit singleton type of static path element
  */
sealed trait PathElement[P]

/** Dynamically set segment within an URI which has a unique name to reference it on input. */
sealed trait SegmentParam[K, V] extends ApiElement

/** Query parameter which represents its key as singleton type and describes the value type. */
sealed trait QueryParam[K, V] extends ApiElement

/** Header which represents its key as singleton type and describes the value type. */
sealed trait HeaderParam[K, V] extends ApiElement

/** Request body type description. */
sealed trait ReqBodyElement[MT <: MediaType, A] extends ApiElement

trait MethodElement extends ApiElement
sealed trait GetElement[MT <: MediaType, A] extends MethodElement
sealed trait PutElement[MT <: MediaType, A] extends MethodElement
sealed trait PutWithBodyElement[BMT <: MediaType, Bd, MT <: MediaType, A] extends MethodElement
sealed trait PostElement[MT <: MediaType, A] extends MethodElement
sealed trait PostWithBodyElement[BMT <: MediaType, Bd, MT <: MediaType, A] extends MethodElement
sealed trait DeleteElement[MT <: MediaType, A] extends MethodElement

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
