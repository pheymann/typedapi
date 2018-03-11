package typedapi.server

import typedapi.shared._
import shapeless.{HList, HNil, Witness}
import shapeless.labelled.FieldType

import scala.util.Try

sealed trait RouteExtractor[-R <: EndpointRequest, El <: HList, In <: HList, EIn <: HList] {

  type Out

  def apply(request: R, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out]
}

object RouteExtractor {

  type Aux[-R <: EndpointRequest, El <: HList, In <: HList, EIn <: HList, Out0] = RouteExtractor[R, El, In, EIn] { type Out = Out0 }
}

trait RouteExtractorLowPrio {

  implicit def pathExtractor[S, El <: HList, In <: HList, EIn <: HList](implicit wit: Witness.Aux[S], next: RouteExtractor[EndpointRequest, El, In, EIn]) = 
    new RouteExtractor[EndpointRequest, shapeless.::[S, El], In, EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] = request.uri match {
      case p :: tail => 
        if (p == wit.value.toString()) 
          next(request.withUri(tail), extractedHeaderKeys, inAgg)
        else
          None

      case Nil => None
    }
  }
}

trait RouteExtractorMediumPrio extends RouteExtractorLowPrio {

  implicit def finalExtractor[EIn <: HList] = new RouteExtractor[EndpointRequest, HNil, HNil, EIn] {
    type Out = EIn

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] = Some(inAgg)
  }

  implicit def segmentExtractor[El <: HList, K, A, In <: HList, EIn <: HList](implicit value: ValueExtractor[A], next: RouteExtractor[EndpointRequest, El, In, shapeless.::[A, EIn]]) = new RouteExtractor[EndpointRequest, shapeless.::[SegmentInput, El], shapeless.::[FieldType[K, A], In], EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] = request.uri match {
      case p :: tail => value(p).flatMap(v => next(request.withUri(tail), extractedHeaderKeys, v :: inAgg))
      case Nil       => None
    }
  }

  implicit def queryExtractor[El <: HList, K <: Symbol, A, In <: HList, EIn <: HList](implicit wit: Witness.Aux[K], value: ValueExtractor[A], next: RouteExtractor[EndpointRequest, El, In, shapeless.::[A, EIn]]) = new RouteExtractor[EndpointRequest, shapeless.::[QueryInput, El], shapeless.::[FieldType[K, A], In], EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] =
      for {
        raw <- request.queries.get(wit.value.name)
        v   <- value(raw)
        out <- next(request, extractedHeaderKeys, v :: inAgg)
      } yield out
  }

  implicit def headerExtractor[El <: HList, K <: Symbol, A, In <: HList, EIn <: HList](implicit wit: Witness.Aux[K], value: ValueExtractor[A], next: RouteExtractor[EndpointRequest, El, In, shapeless.::[A, EIn]]) = new RouteExtractor[EndpointRequest, shapeless.::[HeaderInput, El], shapeless.::[FieldType[K, A], In], EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] =
      for {
        raw <- request.headers.get(wit.value.name)
        v   <- value(raw)
        out <- next(request, extractedHeaderKeys + wit.value.name, v :: inAgg)
      } yield out
  }

  implicit def rawHeaderExtractor[El <: HList, In <: HList, EIn <: HList](implicit next: RouteExtractor[EndpointRequest, El, In, shapeless.::[Map[String, String], EIn]]) = new RouteExtractor[EndpointRequest, shapeless.::[RawHeadersInput, El], shapeless.::[FieldType[RawHeadersField.T, Map[String, String]], In], EIn] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: EIn): Option[Out] = {
      val raw = request.headers.filterKeys(!extractedHeaderKeys(_))

      if (raw.isEmpty)
        None
      else
        next(request, extractedHeaderKeys, raw :: inAgg)
    }
  }

  implicit def getExtractor[El <: HList, In <: HList](implicit next: RouteExtractor[EndpointRequest, El, In, HNil]) = new RouteExtractor[EndpointRequest, shapeless.::[GetCall, El], In, HNil] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: HNil): Option[Out] =
      if (request.method == "GET") 
        next(request, extractedHeaderKeys, inAgg)
      else 
        None
  }

  implicit def putExtractor[El <: HList, In <: HList](implicit next: RouteExtractor[EndpointRequest, El, In, HNil]) = new RouteExtractor[EndpointRequest, shapeless.::[PutCall, El], In, HNil] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: HNil): Option[Out] =
      if (request.method == "PUT") 
        next(request, extractedHeaderKeys, inAgg)
      else 
        None
  }

  implicit def putWithBodyExtractor[Bd, El <: HList, In <: HList](implicit next: RouteExtractor[EndpointRequest, El, In, shapeless.::[Bd, HNil]]) = new RouteExtractor[BodyEndpointRequest[Bd], shapeless.::[PutWithBodyCall[Bd], El], shapeless.::[FieldType[BodyField.T, Bd], In], HNil] {
    type Out = next.Out

    def apply(request: BodyEndpointRequest[Bd], extractedHeaderKeys: Set[String], inAgg: HNil): Option[Out] =
      if (request.method == "PUT") 
        next(request, extractedHeaderKeys, request.body :: inAgg)
      else 
        None
  }

  implicit def postExtractor[El <: HList, In <: HList](implicit next: RouteExtractor[EndpointRequest, El, In, HNil]) = new RouteExtractor[EndpointRequest, shapeless.::[PostCall, El], In, HNil] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: HNil): Option[Out] =
      if (request.method == "POST") 
        next(request, extractedHeaderKeys, inAgg)
      else 
        None
  }

  implicit def postWithBodyExtractor[Bd, El <: HList, In <: HList](implicit next: RouteExtractor[EndpointRequest, El, In, shapeless.::[Bd, HNil]]) = new RouteExtractor[BodyEndpointRequest[Bd], shapeless.::[PostWithBodyCall[Bd], El], shapeless.::[FieldType[BodyField.T, Bd], In], HNil] {
    type Out = next.Out

    def apply(request: BodyEndpointRequest[Bd], extractedHeaderKeys: Set[String], inAgg: HNil): Option[Out] =
      if (request.method == "POST") 
        next(request, extractedHeaderKeys, request.body :: inAgg)
      else 
        None
  }

  implicit def deleteExtractor[El <: HList, In <: HList](implicit next: RouteExtractor[EndpointRequest, El, In, HNil]) = new RouteExtractor[EndpointRequest, shapeless.::[DeleteCall, El], In, HNil] {
    type Out = next.Out

    def apply(request: EndpointRequest, extractedHeaderKeys: Set[String], inAgg: HNil): Option[Out] =
      if (request.method == "DELETE") 
        next(request, extractedHeaderKeys, inAgg)
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
  implicit val shortExtractor   = extract[Short](_.toShort)
  implicit val intExtractor     = extract[Int](_.toInt)
  implicit val longExtractor    = extract[Long](_.toLong)
  implicit val floatExtractor   = extract[Float](_.toFloat)
  implicit val doubleExtractor  = extract[Double](_.toDouble)
  implicit val stringExtractor  = extract[String](identity)
}

final case class BodyType[Bd]()
