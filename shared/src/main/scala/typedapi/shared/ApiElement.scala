package typedapi.shared

import scala.annotation.implicitNotFound

sealed trait ApiElement

/** Type-container providing the singleton-type of an static path element */
sealed trait PathElement[P]

/** Type-container providing the name (singleton) and value type for a path parameter. */
sealed trait SegmentParam[K, V] extends ApiElement

/** Type-container providing the name (singleton) and value type for a query parameter. */
sealed trait QueryParam[K, V] extends ApiElement

/** Type-container providing the name (singleton) and value type for a header parameter. */
sealed trait HeaderParam[K, V] extends ApiElement

/** Type-container providing the name (singleton) and value type for a static header element. */
sealed trait FixedHeaderElement[K, V] extends ApiElement
/** Type-container providing the name (singleton) and value type for a static header element only used for the client. */
sealed trait ClientHeaderElement[K, V] extends ApiElement
/** Type-container providing the name (singleton) and value type for a header parameter only used for the client. */
sealed trait ClientHeaderParam[K, V] extends ApiElement
/** Type-container providing a collection of headers (Map[String, V]) only used for the client. */
sealed trait ClientHeaderCollParam[V] extends ApiElement
/** Type-container providing the name (singleton) and value type for a static header element sent by server. */
sealed trait ServerHeaderSendElement[K, V] extends ApiElement
/** Type-container providing the name (singleton) and value type describing a sub-string headers have to match only used for the server. */
sealed trait ServerHeaderMatchParam[K, V] extends ApiElement

/** Type-container providing the media-type and value type for a request body. */
sealed trait ReqBodyElement[MT <: MediaType, A] extends ApiElement

trait MethodElement extends ApiElement
/** Type-container representing a GET operation with a media-type and value type for the result. */
sealed trait GetElement[MT <: MediaType, A] extends MethodElement
/** Type-container representing a PUT operation with a media-type and value type for the result. */
sealed trait PutElement[MT <: MediaType, A] extends MethodElement
/** Type-container representing a PUT operation with a media-type and value type for the result and a body. */
sealed trait PutWithBodyElement[BMT <: MediaType, Bd, MT <: MediaType, A] extends MethodElement
/** Type-container representing a POST operation with a media-type and value type for the result. */
sealed trait PostElement[MT <: MediaType, A] extends MethodElement
/** Type-container representing a POST operation with a media-type and value type for the result and a body. */
sealed trait PostWithBodyElement[BMT <: MediaType, Bd, MT <: MediaType, A] extends MethodElement
/** Type-container representing a DELETE operation with a media-type and value type for the result. */
sealed trait DeleteElement[MT <: MediaType, A] extends MethodElement

@implicitNotFound("""You try to add a request body to a method which doesn't expect one.

method: ${M}
""")
trait MethodToReqBody[M <: MethodElement, MT <: MediaType, Bd] {

  type Out <: MethodElement
}

trait MethodToReqBodyLowPrio {

  implicit def reqBodyForPut[MT <: MediaType, A, BMT <: MediaType, Bd] = new MethodToReqBody[PutElement[MT, A], BMT, Bd] {
    type Out = PutWithBodyElement[BMT, Bd, MT, A]
  }

  implicit def reqBodyForPost[MT <: MediaType, A, BMT <: MediaType, Bd] = new MethodToReqBody[PostElement[MT, A], BMT, Bd] {
    type Out = PostWithBodyElement[BMT, Bd, MT, A]
  }
}
