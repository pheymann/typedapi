package typedapi.server

import typedapi.shared._
import shapeless.{HList, HNil, Witness}
import shapeless.labelled.FieldType
import shapeless.ops.hlist.Reverse

import scala.util.Try

sealed trait ExtractionError
case object RouteNotFound extends ExtractionError
final case class BadRouteRequest(msg: String) extends ExtractionError

sealed trait RouteExtractor[El <: HList, In <: HList, EIn <: HList] {

  type Out

  def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Either[ExtractionError, Out]
}

object RouteExtractor {

  type Aux[El <: HList, In <: HList, EIn <: HList, Out0] = RouteExtractor[El, In, EIn] { type Out = Out0 }

  type Extract[Out] = Either[ExtractionError, Out]

  def NotFoundE[Out]: Extract[Out] = Left(RouteNotFound)
  def BadRequestE[Out](msg: String): Extract[Out] = Left(BadRouteRequest(msg))
}

trait RouteExtractorLowPrio {

  import RouteExtractor._

  implicit def pathExtractor[S, El <: HList, In <: HList, EIn <: HList](implicit wit: Witness.Aux[S], next: RouteExtractor[El, In, EIn]) = 
    new RouteExtractor[shapeless.::[S, El], In, EIn] {
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

  implicit def segmentExtractor[El <: HList, K, A, In <: HList, EIn <: HList](implicit value: ValueExtractor[A], next: RouteExtractor[El, In, shapeless.::[A, EIn]]) = new RouteExtractor[shapeless.::[SegmentInput, El], shapeless.::[FieldType[K, A], In], EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = request.uri match {
      case p :: tail => value(p).fold(NotFoundE[Out])(v => next(request.copy(uri = tail), extractedHeaderKeys, v :: inAgg))
      case Nil       => NotFoundE
    }
  }

  implicit def queryExtractor[El <: HList, K <: Symbol, A, In <: HList, EIn <: HList](implicit wit: Witness.Aux[K], value: ValueExtractor[A], next: RouteExtractor[El, In, shapeless.::[A, EIn]]) = new RouteExtractor[shapeless.::[QueryInput, El], shapeless.::[FieldType[K, A], In], EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      req.queries.get(wit.value.name).fold(BadRequestE[Out](s"missing query '${wit.value.name}'")) { raw =>
        raw.headOption.flatMap(value.apply).fold(BadRequestE[Out](s"query '${wit.value.name}' has not type ${value.typeDesc}")) { v =>
          next(request, extractedHeaderKeys, v :: inAgg)
        }
      }
    }
  }

  implicit def queryOptListExtractor[El <: HList, K <: Symbol, A, In <: HList, EIn <: HList](implicit wit: Witness.Aux[K], value: ValueExtractor[A], next: RouteExtractor[El, In, shapeless.::[Option[A], EIn]]) = new RouteExtractor[shapeless.::[QueryInput, El], shapeless.::[FieldType[K, Option[A]], In], EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      req.queries.get(wit.value.name).fold(next(request, extractedHeaderKeys, None :: inAgg)) { raw =>
        raw.headOption.flatMap(value.apply).fold(BadRequestE[Out](s"query '${wit.value.name}' has not type ${value.typeDesc}")) { v =>
          next(request, extractedHeaderKeys, Some(v) :: inAgg)
        }
      }
    }
  }

  implicit def queryListExtractor[El <: HList, K <: Symbol, A, In <: HList, EIn <: HList](implicit wit: Witness.Aux[K], value: ValueExtractor[A], next: RouteExtractor[El, In, shapeless.::[List[A], EIn]]) = new RouteExtractor[shapeless.::[QueryInput, El], shapeless.::[FieldType[K, List[A]], In], EIn] {
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

  implicit def headerExtractor[El <: HList, K <: Symbol, A, In <: HList, EIn <: HList](implicit wit: Witness.Aux[K], value: ValueExtractor[A], next: RouteExtractor[El, In, shapeless.::[A, EIn]]) = new RouteExtractor[shapeless.::[HeaderInput, El], shapeless.::[FieldType[K, A], In], EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      req.headers.get(wit.value.name).fold(BadRequestE[Out](s"missing header '${wit.value.name}'")) { raw =>
        value(raw).fold(BadRequestE[Out](s"header '${wit.value.name}' has not type ${value.typeDesc}")) { v =>
          next(request, extractedHeaderKeys + wit.value.name, v :: inAgg)
        }
      }
    }
  }

  implicit def headerOptExtractor[El <: HList, K <: Symbol, A, In <: HList, EIn <: HList](implicit wit: Witness.Aux[K], value: ValueExtractor[A], next: RouteExtractor[El, In, shapeless.::[Option[A], EIn]]) = new RouteExtractor[shapeless.::[HeaderInput, El], shapeless.::[FieldType[K, Option[A]], In], EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      req.headers.get(wit.value.name).fold(next(request, extractedHeaderKeys + wit.value.name, None :: inAgg)) { raw =>
        value(raw).fold(BadRequestE[Out](s"header '${wit.value.name}' has not type ${value.typeDesc}")) { v =>
          next(request, extractedHeaderKeys + wit.value.name, Some(v) :: inAgg)
        }
      }
    }
  }

  implicit def rawHeaderExtractor[El <: HList, In <: HList, EIn <: HList](implicit next: RouteExtractor[El, In, shapeless.::[Map[String, String], EIn]]) = new RouteExtractor[shapeless.::[RawHeadersInput, El], shapeless.::[FieldType[RawHeadersField.T, Map[String, String]], In], EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      val raw = req.headers.filterKeys(!extractedHeaderKeys(_))

      if (raw.isEmpty)
        BadRequestE("no raw headers left, but at least one expected")
      else
        next(request.copy(headers = Map.empty), extractedHeaderKeys, raw :: inAgg)
    }
  }

  implicit def getExtractor[EIn <: HList, REIn <: HList](implicit rev: Reverse.Aux[EIn, REIn]) = new RouteExtractor[shapeless.::[GetCall, HNil], HNil, EIn] {
    type Out = REIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "GET") 
        Right(inAgg.reverse)
      else 
        NotFoundE
    }
  }

  implicit def putExtractor[EIn <: HList, REIn <: HList](implicit rev: Reverse.Aux[EIn, REIn]) = new RouteExtractor[shapeless.::[PutCall, HNil], HNil, EIn] {
    type Out = REIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "PUT") 
        Right(inAgg.reverse)
      else 
        NotFoundE
    }
  }

  implicit def putWithBodyExtractor[Bd, EIn <: HList] = new RouteExtractor[shapeless.::[PutWithBodyCall[Bd], HNil], shapeless.::[FieldType[BodyField.T, Bd], HNil], EIn] {
    type Out = (BodyType[Bd], EIn)

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "PUT") 
        Right((BodyType[Bd], inAgg))
      else 
        NotFoundE
    }
  }

  implicit def postExtractor[EIn <: HList] = new RouteExtractor[shapeless.::[PostCall, HNil], HNil, EIn] {
    type Out = EIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "POST") 
        Right(inAgg)
      else 
        NotFoundE
    }
  }

  implicit def postWithBodyExtractor[Bd, EIn <: HList, REIn <: HList] = new RouteExtractor[shapeless.::[PostWithBodyCall[Bd], HNil], shapeless.::[FieldType[BodyField.T, Bd], HNil], EIn] {
    type Out = (BodyType[Bd], EIn)

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Extract[Out] = checkEmptyPath(request) { req =>
      if (req.method == "POST") 
        Right((BodyType[Bd], inAgg))
      else 
        NotFoundE
    }
  }

  implicit def deleteExtractor[EIn <: HList] = new RouteExtractor[shapeless.::[DeleteCall, HNil], HNil, EIn] {
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
