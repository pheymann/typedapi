package typedapi.server

final case class SuccessCode(statusCode: Int) extends AnyVal
final case class ErrorCode(statusCode: Int) extends AnyVal

final case class HttpError(code: ErrorCode, message: String)

trait EndpointResult {

  type Result[A] = Either[HttpError, (SuccessCode, A)]

  final def successWith[A](code: SuccessCode)(a: A): Result[A] = Right(code -> a)
  final def success[A](a: A): Result[A] = successWith(Ok)(a)

  final val Continue           = SuccessCode(100)
  final val SwitchingProtocols = SuccessCode(101)
  final val Processing         = SuccessCode(102)

  final val Ok                          = SuccessCode(200)
  final val Created                     = SuccessCode(201)
  final val Accepted                    = SuccessCode(202)
  final val NonAuthoritativeInformation = SuccessCode(203)
  final val NoContent                   = SuccessCode(204)
  final val ResetContent                = SuccessCode(205)
  final val PartialContent              = SuccessCode(206)
  final val MultiStatus                 = SuccessCode(207)
  final val AlreadyReported             = SuccessCode(208)
  final val IMUsed                      = SuccessCode(226)

  final val MultipleChoices   = SuccessCode(300)
  final val MovedPermanently  = SuccessCode(301)
  final val Found             = SuccessCode(302)
  final val SeeOther          = SuccessCode(303)
  final val NotModified       = SuccessCode(304)
  final val UseProxy          = SuccessCode(305)
  final val TemporaryRedirect = SuccessCode(307)
  final val PermanentRedirect = SuccessCode(308)

  final def errorWith[A](code: ErrorCode, message: String): Result[A] = Left(HttpError(code, message))

  final val BadRequest                      = ErrorCode(400)
  final val Unauthorized                    = ErrorCode(401)
  final val PaymentRequired                 = ErrorCode(402)
  final val Forbidden                       = ErrorCode(403)
  final val NotFound                        = ErrorCode(404)
  final val MethodNotAllowed                = ErrorCode(405)
  final val NotAcceptable                   = ErrorCode(406)
  final val ProxyAuthenticationRequired     = ErrorCode(407)
  final val RequestTimeout                  = ErrorCode(408)
  final val Conflict                        = ErrorCode(409)
  final val Gone                            = ErrorCode(410)
  final val LengthRequired                  = ErrorCode(411)
  final val PreconditionFailed              = ErrorCode(412)
  final val PayloadTooLarge                 = ErrorCode(413)
  final val RequestURITooLong               = ErrorCode(414)
  final val UnsupportedMediaType            = ErrorCode(415)
  final val RequestedRangeNotSatisfiable    = ErrorCode(416)
  final val ExpectationFailed               = ErrorCode(417)
  final val ImAteapot                       = ErrorCode(418)
  final val MisdirectedRequest              = ErrorCode(421)
  final val UnprocessableEntity             = ErrorCode(422)
  final val Locked                          = ErrorCode(423)
  final val FailedDependency                = ErrorCode(424)
  final val UpgradeRequired                 = ErrorCode(426)
  final val PreconditionRequired            = ErrorCode(428)
  final val TooManyRequests                 = ErrorCode(429)
  final val RequestHeaderFieldsTooLarge     = ErrorCode(431)
  final val ConnectionClosedWithoutResult = ErrorCode(444)
  final val UnavailableForLegalReasons      = ErrorCode(451)
  final val ClientClosedRequest             = ErrorCode(499)

  final val InternalServerError           = ErrorCode(500)
  final val NotImplemented                = ErrorCode(501)
  final val BadGateway                    = ErrorCode(502)
  final val ServiceUnavailable            = ErrorCode(503)
  final val GatewayTimeout                = ErrorCode(504)
  final val HTTPVersionNotSupported       = ErrorCode(505)
  final val VariantAlsoNegotiates         = ErrorCode(506)
  final val InsufficientStorage           = ErrorCode(507)
  final val LoopDetected                  = ErrorCode(508)
  final val NotExtended                   = ErrorCode(510)
  final val NetworkAuthenticationRequired = ErrorCode(511)
  final val NetworkConnectTimeoutError    = ErrorCode(599)
}
