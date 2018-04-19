package typedapi.server

import typedapi.shared._
import shapeless.{HList, HNil, Witness}
import shapeless.ops.hlist.Reverse

import scala.util.Try
import scala.annotation.implicitNotFound

sealed trait ExtractionError
case object RouteNotFound extends ExtractionError
final case class BadRouteRequest(msg: String) extends ExtractionError

/** Build a function which extracts inputs from a given requests based on the API. 
  *  - if a request path does not fit the API definition `RouteNotFound` is returned
  *  - if a query, header, body, etc is missing `BadRouteRequest` is returned
  */
@implicitNotFound("""Cannot find RouteExtractor. Maybe a ValueExtractor could not be found.

elements: ${El}
input keys: ${KIn}
inout values: ${VIn}""")
trait RouteExtractor[El <: HList, KIn <: HList, VIn <: HList, EIn <: HList] {

  type Out

  def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Either[ExtractionError, Out]
}

object RouteExtractor {

  type Aux[El <: HList, KIn <: HList, VIn <: HList, EIn <: HList, Out0] = RouteExtractor[El, KIn, VIn, EIn] { type Out = Out0 }

  type Extract[Out] = Either[ExtractionError, Out]

  def NotFoundE[Out]: Extract[Out] = Left(RouteNotFound)
  def BadRequestE[Out](msg: String): Extract[Out] = Left(BadRouteRequest(msg))
}

trait RouteExtractorLowPrio {

  import RouteExtractor._

  implicit def pathExtractor[S, El <: HList, KIn <: HList, VIn <: HList, EIn <: HList](implicit wit: Witness.Aux[S], next: RouteExtractor[El, KIn, VIn, EIn]) = 
    new RouteExtractor[shapeless.::[S, El], KIn, VIn, EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = request.uri match {
      case p :: tail => 
        if (p == wit.value.toString()) 
          next(request.copy(uri = tail), extractedHeaderKeys, inAgg)
        else
          NotFoundE

      case Nil => NotFoundE
    }
  }
}

trait RouteExtractorMediumPrio extends RouteExtractorLowPrio {

  import RouteExtractor._

  private def checkEmptyPath[Out](request: EndpointRequest)(f: EndpointRequest => Extract[Out]): Extract[Out] = 
    if (request.uri.isEmpty)
      f(request)
    else
      NotFoundE

  implicit def segmentExtractor[El <: HList, K, V, KIn <: HList, VIn <: HList, EIn <: HList](implicit value: ValueExtractor[V], next: RouteExtractor[El, KIn, VIn, shapeless.::[V, EIn]]) = 
    new RouteExtractor[shapeless.::[SegmentInput, El], shapeless.::[K, KIn], shapeless.::[V, VIn], EIn] {
      type Out = next.Out

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = request.uri match {
        case p :: tail => value(p).fold(NotFoundE[Out])(v => next(request.copy(uri = tail), extractedHeaderKeys, v :: inAgg))
        case Nil       => NotFoundE
      }
    }

  implicit def queryExtractor[El <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, EIn <: HList](implicit wit: Witness.Aux[K], value: ValueExtractor[V], next: RouteExtractor[El, KIn, VIn, shapeless.::[V, EIn]]) = 
    new RouteExtractor[shapeless.::[QueryInput, El], shapeless.::[K, KIn], shapeless.::[V, VIn], EIn] {
      type Out = next.Out

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
        req.queries.get(wit.value.name).fold(BadRequestE[Out](s"missing query '${wit.value.name}'")) { raw =>
          raw.headOption.flatMap(value.apply).fold(BadRequestE[Out](s"query '${wit.value.name}' has not type ${value.typeDesc}")) { v =>
            next(request, extractedHeaderKeys, v :: inAgg)
          }
        }
      }
    }

  implicit def queryOptListExtractor[El <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, EIn <: HList](implicit wit: Witness.Aux[K], value: ValueExtractor[V], next: RouteExtractor[El, KIn, VIn, shapeless.::[Option[V], EIn]]) = 
    new RouteExtractor[shapeless.::[QueryInput, El], shapeless.::[K, KIn], shapeless.::[Option[V], VIn], EIn] {
      type Out = next.Out

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
        req.queries.get(wit.value.name).fold(next(request, extractedHeaderKeys, None :: inAgg)) { raw =>
          raw.headOption.flatMap(value.apply).fold(BadRequestE[Out](s"query '${wit.value.name}' has not type ${value.typeDesc}")) { v =>
            next(request, extractedHeaderKeys, Some(v) :: inAgg)
          }
        }
      }
    }

  implicit def queryListExtractor[El <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, EIn <: HList](implicit wit: Witness.Aux[K], value: ValueExtractor[V], next: RouteExtractor[El, KIn, VIn, shapeless.::[List[V], EIn]]) = 
    new RouteExtractor[shapeless.::[QueryInput, El], shapeless.::[K, KIn], shapeless.::[List[V], VIn], EIn] {
      type Out = next.Out

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
        req.queries.get(wit.value.name).fold(next(request, extractedHeaderKeys, Nil :: inAgg)) { raw =>
          val vs = raw.flatMap(value.apply)

          if (vs.length < raw.length)
            BadRequestE(s"some values of query '${wit.value.name}' are no ${value.typeDesc}")
          else
            next(request, extractedHeaderKeys, vs :: inAgg)
        }
      }
    }

  implicit def headerExtractor[El <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, EIn <: HList](implicit wit: Witness.Aux[K], value: ValueExtractor[V], next: RouteExtractor[El, KIn, VIn, shapeless.::[V, EIn]]) = 
    new RouteExtractor[shapeless.::[HeaderInput, El], shapeless.::[K, KIn], shapeless.::[V, VIn], EIn] {
      type Out = next.Out

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
        req.headers.get(wit.value.name).fold(BadRequestE[Out](s"missing header '${wit.value.name}'")) { raw =>
          value(raw).fold(BadRequestE[Out](s"header '${wit.value.name}' has not type ${value.typeDesc}")) { v =>
            next(request, extractedHeaderKeys + wit.value.name, v :: inAgg)
          }
        }
      }
    }

  implicit def headerOptExtractor[El <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, EIn <: HList](implicit wit: Witness.Aux[K], value: ValueExtractor[V], next: RouteExtractor[El, KIn, VIn, shapeless.::[Option[V], EIn]]) = 
    new RouteExtractor[shapeless.::[HeaderInput, El], shapeless.::[K, KIn], shapeless.::[Option[V], VIn], EIn] {
      type Out = next.Out

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
        req.headers.get(wit.value.name).fold(next(request, extractedHeaderKeys + wit.value.name, None :: inAgg)) { raw =>
          value(raw).fold(BadRequestE[Out](s"header '${wit.value.name}' has not type ${value.typeDesc}")) { v =>
            next(request, extractedHeaderKeys + wit.value.name, Some(v) :: inAgg)
          }
        }
      }
    }

  implicit def rawHeaderExtractor[El <: HList, KIn <: HList, VIn <: HList, EIn <: HList](implicit next: RouteExtractor[El, KIn, VIn, shapeless.::[Map[String, String], EIn]]) = 
    new RouteExtractor[shapeless.::[RawHeadersInput, El], shapeless.::[RawHeadersField.T, KIn], shapeless.::[Map[String, String], VIn], EIn] {
      type Out = next.Out

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
        val raw = req.headers.filterKeys(!extractedHeaderKeys(_))

        if (raw.isEmpty)
          BadRequestE("no raw headers left, but at least one expected")
        else
          next(request.copy(headers = Map.empty), extractedHeaderKeys, raw :: inAgg)
      }
    }

  implicit def getExtractor[EIn <: HList, REIn <: HList](implicit rev: Reverse.Aux[EIn, REIn]) = new RouteExtractor[shapeless.::[GetCall, HNil], HNil, HNil, EIn] {
    type Out = REIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "GET") 
        Right(inAgg.reverse)
      else 
        NotFoundE
    }
  }

  implicit def putExtractor[EIn <: HList, REIn <: HList](implicit rev: Reverse.Aux[EIn, REIn]) = new RouteExtractor[shapeless.::[PutCall, HNil], HNil, HNil, EIn] {
    type Out = REIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "PUT") 
        Right(inAgg.reverse)
      else 
        NotFoundE
    }
  }

  implicit def putWithBodyExtractor[Bd, EIn <: HList] = new RouteExtractor[shapeless.::[PutWithBodyCall[Bd], HNil], shapeless.::[BodyField.T, HNil], shapeless.::[Bd, HNil], EIn] {
    type Out = (BodyType[Bd], EIn)

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "PUT") 
        Right((BodyType[Bd], inAgg))
      else 
        NotFoundE
    }
  }

  implicit def postExtractor[EIn <: HList] = new RouteExtractor[shapeless.::[PostCall, HNil], HNil, HNil, EIn] {
    type Out = EIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "POST") 
        Right(inAgg)
      else 
        NotFoundE
    }
  }

  implicit def postWithBodyExtractor[Bd, EIn <: HList, REIn <: HList] = new RouteExtractor[shapeless.::[PostWithBodyCall[Bd], HNil], shapeless.::[BodyField.T, HNil], shapeless.::[Bd, HNil], EIn] {
    type Out = (BodyType[Bd], EIn)

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "POST") 
        Right((BodyType[Bd], inAgg))
      else 
        NotFoundE
    }
  }

  implicit def deleteExtractor[EIn <: HList] = new RouteExtractor[shapeless.::[DeleteCall, HNil], HNil, HNil, EIn] {
    type Out = EIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "DELETE") 
        Right(inAgg)
      else 
        NotFoundE
    }
  }
}

sealed trait ValueExtractor[A] extends (String => Option[A]) {

  def typeDesc: String
}

trait ValueExtractorInstances {

  def extract[A](f: String => A, _typeDesc: String) = new ValueExtractor[A] {
    val typeDesc = _typeDesc

    def apply(raw: String): Option[A] = Try(f(raw)).toOption
  }

  implicit val booleanExtractor = extract[Boolean](_.toBoolean, "Boolean")
  implicit val byteExtractor    = extract[Byte](_.toByte, "Byte")
  implicit val shortExtractor   = extract[Short](_.toShort, "Short")
  implicit val intExtractor     = extract[Int](_.toInt, "Int")
  implicit val longExtractor    = extract[Long](_.toLong, "Long")
  implicit val floatExtractor   = extract[Float](_.toFloat, "Float")
  implicit val doubleExtractor  = extract[Double](_.toDouble, "Double")
  implicit val stringExtractor  = extract[String](identity, "String")
}

final case class BodyType[Bd]()
