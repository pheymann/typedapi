package typedapi.server

import typedapi.shared._
import shapeless.{HList, HNil, Witness}
import shapeless.labelled.FieldType
import shapeless.ops.hlist.Reverse

import scala.util.Try

sealed trait RouteExtractor[El <: HList, In <: HList, EIn <: HList] {

  type Out

  def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out]
}

object RouteExtractor {

  type Aux[El <: HList, In <: HList, EIn <: HList, Out0] = RouteExtractor[El, In, EIn] { type Out = Out0 }
}

trait RouteExtractorLowPrio {

  implicit def pathExtractor[S, El <: HList, In <: HList, EIn <: HList](implicit wit: Witness.Aux[S], next: RouteExtractor[El, In, EIn]) = 
    new RouteExtractor[shapeless.::[S, El], In, EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] = request.uri match {
      case p :: tail => 
        if (p == wit.value.toString()) 
          next(request.copy(uri = tail), extractedHeaderKeys, inAgg)
        else
          None

      case Nil => None
    }
  }
}

trait RouteExtractorMediumPrio extends RouteExtractorLowPrio {

  implicit def segmentExtractor[El <: HList, K, A, In <: HList, EIn <: HList](implicit value: ValueExtractor[A], next: RouteExtractor[El, In, shapeless.::[A, EIn]]) = new RouteExtractor[shapeless.::[SegmentInput, El], shapeless.::[FieldType[K, A], In], EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] = request.uri match {
      case p :: tail => value(p).flatMap(v => next(request.copy(uri = tail), extractedHeaderKeys, v :: inAgg))
      case Nil       => None
    }
  }

  implicit def queryExtractor[El <: HList, K <: Symbol, A, In <: HList, EIn <: HList](implicit wit: Witness.Aux[K], value: ValueExtractor[A], next: RouteExtractor[El, In, shapeless.::[A, EIn]]) = new RouteExtractor[shapeless.::[QueryInput, El], shapeless.::[FieldType[K, A], In], EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] =
      for {
        raw <- request.queries.get(wit.value.name)
        v   <- value(raw)
        out <- next(request, extractedHeaderKeys, v :: inAgg)
      } yield out
  }

  implicit def headerExtractor[El <: HList, K <: Symbol, A, In <: HList, EIn <: HList](implicit wit: Witness.Aux[K], value: ValueExtractor[A], next: RouteExtractor[El, In, shapeless.::[A, EIn]]) = new RouteExtractor[shapeless.::[HeaderInput, El], shapeless.::[FieldType[K, A], In], EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] =
      for {
        raw <- request.headers.get(wit.value.name)
        v   <- value(raw)
        out <- next(request, extractedHeaderKeys + wit.value.name, v :: inAgg)
      } yield out
  }

  implicit def rawHeaderExtractor[El <: HList, In <: HList, EIn <: HList](implicit next: RouteExtractor[El, In, shapeless.::[Map[String, String], EIn]]) = new RouteExtractor[shapeless.::[RawHeadersInput, El], shapeless.::[FieldType[RawHeadersField.T, Map[String, String]], In], EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] = {
      val raw = request.headers.filterKeys(!extractedHeaderKeys(_))

      if (raw.isEmpty)
        None
      else
        next(request.copy(headers = Map.empty), extractedHeaderKeys, raw :: inAgg)
    }
  }

  implicit def getExtractor[EIn <: HList, REIn <: HList](implicit rev: Reverse.Aux[EIn, REIn]) = new RouteExtractor[shapeless.::[GetCall, HNil], HNil, EIn] {
    type Out = REIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] =
      if (request.uri.isEmpty && request.method == "GET") 
        Some(inAgg.reverse)
      else 
        None
  }

  implicit def putExtractor[EIn <: HList, REIn <: HList](implicit rev: Reverse.Aux[EIn, REIn]) = new RouteExtractor[shapeless.::[PutCall, HNil], HNil, EIn] {
    type Out = REIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] =
      if (request.uri.isEmpty && request.method == "PUT") 
        Some(inAgg.reverse)
      else 
        None
  }

  implicit def putWithBodyExtractor[Bd, EIn <: HList] = new RouteExtractor[shapeless.::[PutWithBodyCall[Bd], HNil], shapeless.::[FieldType[BodyField.T, Bd], HNil], EIn] {
    type Out = (BodyType[Bd], EIn)

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] =
      if (request.uri.isEmpty && request.method == "PUT") 
        Some((BodyType[Bd], inAgg))
      else 
        None
  }

  implicit def postExtractor[EIn <: HList] = new RouteExtractor[shapeless.::[PostCall, HNil], HNil, EIn] {
    type Out = EIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] =
      if (request.uri.isEmpty && request.method == "POST") 
        Some(inAgg)
      else 
        None
  }

  implicit def postWithBodyExtractor[Bd, EIn <: HList, REIn <: HList] = new RouteExtractor[shapeless.::[PostWithBodyCall[Bd], HNil], shapeless.::[FieldType[BodyField.T, Bd], HNil], EIn] {
    type Out = (BodyType[Bd], EIn)

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] =
      if (request.uri.isEmpty && request.method == "POST") 
        Some((BodyType[Bd], inAgg))
      else 
        None
  }

  implicit def deleteExtractor[EIn <: HList] = new RouteExtractor[shapeless.::[DeleteCall, HNil], HNil, EIn] {
    type Out = EIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] =
      if (request.uri.isEmpty && request.method == "DELETE") 
        Some(inAgg)
      else 
        None
  }
}

sealed trait ValueExtractor[A] extends (String => Option[A])

trait ValueExtractorInstances {

  def extract[A](f: String => A) = new ValueExtractor[A] {
    def apply(raw: String): Option[A] = Try(f(raw)).toOption
  }

  implicit val booleanExtractor = extract[Boolean](_.toBoolean)
  implicit val byteExtractor    = extract[Byte](_.toByte)
  implicit val shortExtractor   = extract[Short](_.toShort)
  implicit val intExtractor     = extract[Int](_.toInt)
  implicit val longExtractor    = extract[Long](_.toLong)
  implicit val floatExtractor   = extract[Float](_.toFloat)
  implicit val doubleExtractor  = extract[Double](_.toDouble)
  implicit val stringExtractor  = extract[String](identity)
}

final case class BodyType[Bd]()
