package typedapi.server

import typedapi.shared._
import shapeless.{HList, HNil, Witness}
import shapeless.labelled.FieldType
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
inout values: ${VIn}
method: ${M}""")
trait RouteExtractor[El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, EIn <: HList] {

  type Out

  def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Either[ExtractionError, Out]
}

object RouteExtractor extends RouteExtractorMediumPrio {

  type Aux[El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, EIn <: HList, Out0] = RouteExtractor[El, KIn, VIn, M, EIn] { type Out = Out0 }

  type Extract[Out] = Either[ExtractionError, Out]

  def NotFoundE[Out]: Extract[Out] = Left(RouteNotFound)
  def BadRequestE[Out](msg: String): Extract[Out] = Left(BadRouteRequest(msg))
}

trait RouteExtractorLowPrio {

  import RouteExtractor._

  implicit def pathExtractor[S, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, EIn <: HList]
      (implicit wit: Witness.Aux[S], show: WitnessToString[S], next: RouteExtractor[El, KIn, VIn, M, EIn]) =
    new RouteExtractor[shapeless.::[S, El], KIn, VIn, M, EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = request.uri match {
      case p :: tail => 
        if (p == show.show(wit.value))
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

  implicit def segmentExtractor[El <: HList, K, V, KIn <: HList, VIn <: HList, M <: MethodType, EIn <: HList](implicit value: ValueExtractor[V], next: RouteExtractor[El, KIn, VIn, M, shapeless.::[V, EIn]]) = 
    new RouteExtractor[shapeless.::[SegmentInput, El], shapeless.::[K, KIn], shapeless.::[V, VIn], M, EIn] {
      type Out = next.Out

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = request.uri match {
        case p :: tail => value(p).fold(NotFoundE[Out])(v => next(request.copy(uri = tail), extractedHeaderKeys, v :: inAgg))
        case Nil       => NotFoundE
      }
    }

  implicit def queryExtractor[El <: HList, K, V, KIn <: HList, VIn <: HList, M <: MethodType, EIn <: HList]
      (implicit wit: Witness.Aux[K], show: WitnessToString[K], value: ValueExtractor[V], next: RouteExtractor[El, KIn, VIn, M, shapeless.::[V, EIn]]) =
    new RouteExtractor[shapeless.::[QueryInput, El], shapeless.::[K, KIn], shapeless.::[V, VIn], M, EIn] {
      type Out = next.Out

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
        val key = show.show(wit.value)

        req.queries.get(key).fold(BadRequestE[Out](s"missing query '$key'")) { raw =>
          raw.headOption.flatMap(value.apply).fold(BadRequestE[Out](s"query '$key' has not type ${value.typeDesc}")) { v =>
            next(request, extractedHeaderKeys, v :: inAgg)
          }
        }
      }
    }

  implicit def queryOptListExtractor[El <: HList, K, V, KIn <: HList, VIn <: HList, M <: MethodType, EIn <: HList]
      (implicit wit: Witness.Aux[K], show: WitnessToString[K], value: ValueExtractor[V], next: RouteExtractor[El, KIn, VIn, M, shapeless.::[Option[V], EIn]]) =
    new RouteExtractor[shapeless.::[QueryInput, El], shapeless.::[K, KIn], shapeless.::[Option[V], VIn], M, EIn] {
      type Out = next.Out

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
        val key = show.show(wit.value)

        req.queries.get(key).fold(next(request, extractedHeaderKeys, None :: inAgg)) { raw =>
          raw.headOption.flatMap(value.apply).fold(BadRequestE[Out](s"query '$key' has not type ${value.typeDesc}")) { v =>
            next(request, extractedHeaderKeys, Some(v) :: inAgg)
          }
        }
      }
    }

  implicit def queryListExtractor[El <: HList, K, V, KIn <: HList, VIn <: HList, M <: MethodType, EIn <: HList]
      (implicit wit: Witness.Aux[K], show: WitnessToString[K], value: ValueExtractor[V], next: RouteExtractor[El, KIn, VIn, M, shapeless.::[List[V], EIn]]) =
    new RouteExtractor[shapeless.::[QueryInput, El], shapeless.::[K, KIn], shapeless.::[List[V], VIn], M, EIn] {
      type Out = next.Out

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
        val key = show.show(wit.value)

        req.queries.get(key).fold(next(request, extractedHeaderKeys, Nil :: inAgg)) { raw =>
          val vs = raw.flatMap(value.apply)

          if (vs.length < raw.length)
            BadRequestE(s"some values of query '$key' are no ${value.typeDesc}")
          else
            next(request, extractedHeaderKeys, vs :: inAgg)
        }
      }
    }

  implicit def headerExtractor[El <: HList, K, V, KIn <: HList, VIn <: HList, M <: MethodType, EIn <: HList]
      (implicit wit: Witness.Aux[K], show: WitnessToString[K], value: ValueExtractor[V], next: RouteExtractor[El, KIn, VIn, M, shapeless.::[V, EIn]]) =
    new RouteExtractor[shapeless.::[HeaderInput, El], shapeless.::[K, KIn], shapeless.::[V, VIn], M, EIn] {
      type Out = next.Out

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
        val key = show.show(wit.value).toLowerCase

        req.headers.get(key).fold(BadRequestE[Out](s"missing header '$key'")) { raw =>
          value(raw).fold(BadRequestE[Out](s"header '$key' has not type ${value.typeDesc}")) { v =>
            next(request, extractedHeaderKeys + key, v :: inAgg)
          }
        }
      }
    }

  implicit def headerOptExtractor[El <: HList, K, V, KIn <: HList, VIn <: HList, M <: MethodType, EIn <: HList]
      (implicit wit: Witness.Aux[K], show: WitnessToString[K], value: ValueExtractor[V], next: RouteExtractor[El, KIn, VIn, M, shapeless.::[Option[V], EIn]]) =
    new RouteExtractor[shapeless.::[HeaderInput, El], shapeless.::[K, KIn], shapeless.::[Option[V], VIn], M, EIn] {
      type Out = next.Out

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
        val key = show.show(wit.value)

        req.headers.get(key.toLowerCase).fold(next(request, extractedHeaderKeys + key, None :: inAgg)) { raw =>
          value(raw).fold(BadRequestE[Out](s"header '$key' has not type ${value.typeDesc}")) { v =>
            next(request, extractedHeaderKeys + key, Some(v) :: inAgg)
          }
        }
      }
    }

  implicit def fixedHeaderExtractor[K, V, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, EIn <: HList]
      (implicit kWit: Witness.Aux[K], kShow: WitnessToString[K], vWit: Witness.Aux[V], vShow: WitnessToString[V], next: RouteExtractor[El, KIn, VIn, M, EIn]) =
    new RouteExtractor[shapeless.::[FixedHeader[K, V], El], KIn, VIn, M, EIn] {
      type Out = next.Out

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
        val key   = kShow.show(kWit.value)
        val value = vShow.show(vWit.value)

        req.headers.get(key.toLowerCase).fold(BadRequestE[Out](s"missing header '$key'")) { raw =>
          if (raw != value)
            BadRequestE[Out](s"header '$key' has unexpected value '${raw}' - expected '${value}'")
          else
            next(request, extractedHeaderKeys + key, inAgg)
        }
      }
    }

  implicit def getExtractor[EIn <: HList, REIn <: HList](implicit rev: Reverse.Aux[EIn, REIn]) = new RouteExtractor[HNil, HNil, HNil, GetCall, EIn] {
    type Out = REIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "GET") 
        Right(inAgg.reverse)
      else 
        NotFoundE
    }
  }

  implicit def putExtractor[EIn <: HList, REIn <: HList](implicit rev: Reverse.Aux[EIn, REIn]) = new RouteExtractor[HNil, HNil, HNil, PutCall, EIn] {
    type Out = REIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "PUT") 
        Right(inAgg.reverse)
      else 
        NotFoundE
    }
  }

  implicit def putWithBodyExtractor[BMT <: MediaType, Bd, EIn <: HList] = 
    new RouteExtractor[HNil, shapeless.::[FieldType[BMT, BodyField.T], HNil], shapeless.::[Bd, HNil], PutWithBodyCall, EIn] {
      type Out = (BodyType[Bd], EIn)

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
        if (req.method == "PUT")
          Right((BodyType[Bd], inAgg))
        else
          NotFoundE
      }
    }

  implicit def postExtractor[EIn <: HList] = new RouteExtractor[HNil, HNil, HNil, PostCall, EIn] {
    type Out = EIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "POST") 
        Right(inAgg)
      else 
        NotFoundE
    }
  }

  implicit def postWithBodyExtractor[BMT <: MediaType, Bd, EIn <: HList, REIn <: HList] = 
    new RouteExtractor[HNil, shapeless.::[FieldType[BMT, BodyField.T], HNil], shapeless.::[Bd, HNil], PostWithBodyCall, EIn] {
      type Out = (BodyType[Bd], EIn)

      def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
        if (req.method == "POST")
          Right((BodyType[Bd], inAgg))
        else
          NotFoundE
      }
    }

  implicit def deleteExtractor[EIn <: HList] = new RouteExtractor[HNil, HNil, HNil, DeleteCall, EIn] {
    type Out = EIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "DELETE") 
        Right(inAgg)
      else 
        NotFoundE
    }
  }
}

trait ValueExtractor[A] extends (String => Option[A]) {

  def typeDesc: String
}

object ValueExtractor extends ValueExtractorInstances

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
