package typedapi.server

import typedapi.dsl._
import typedapi.shared._
import shapeless.{HList, HNil, Lazy}
import org.specs2.mutable.Specification

final class RouteExtractorSpec extends Specification {

  case class Foo(name: String)

  def extract[H <: HList, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, ROut, Out]
    (api: ApiTypeCarrier[H])
    (implicit folder: Lazy[TplLeftFolder.Aux[ApiTransformer.type, H, Unit, (El, KIn, VIn, M, Out)]],
              extractor: RouteExtractor.Aux[El, KIn, VIn, M, HNil, ROut]): RouteExtractor.Aux[El, KIn, VIn, M, HNil, ROut] = extractor

  "determine routes defined by requests and extract included data (segments, queries, headers)" >> {
    "no data" >> {
      val ext = extract(:= :> "hello" :> "world" :> Get[Json, Foo])
      
      ext(EndpointRequest("GET", List("hello", "world"), Map.empty, Map.empty), HNil) === Right(HNil)
      ext(EndpointRequest("GET", List("hello", "wrong"), Map.empty, Map.empty), HNil) === RouteExtractor.NotFoundE
      ext(EndpointRequest("GET", List("hello"), Map.empty, Map.empty), HNil) === RouteExtractor.NotFoundE
      ext(EndpointRequest("GET", Nil, Map.empty, Map.empty), HNil) === RouteExtractor.NotFoundE
    }

    "segments" >> {
      val ext = extract(:= :> "foo" :> Segment[Int]('age) :> Get[Json, Foo])

      ext(EndpointRequest("GET", List("foo", "0"), Map.empty, Map.empty), HNil) === Right(0 :: HNil)
      ext(EndpointRequest("GET", List("foo", "wrong"), Map.empty, Map.empty), HNil) === RouteExtractor.NotFoundE
      ext(EndpointRequest("GET", List("foo"), Map.empty, Map.empty), HNil) === RouteExtractor.NotFoundE
      ext(EndpointRequest("GET", Nil, Map.empty, Map.empty), HNil) === RouteExtractor.NotFoundE
    }

    "queries" >> {
      val ext0 = extract(:= :> "foo" :> Query[Int]('age) :> Get[Json, Foo])

      ext0(EndpointRequest("GET", List("foo"), Map("age" -> List("0")), Map.empty), HNil) === Right(0 :: HNil)
      ext0(EndpointRequest("GET", List("foo"), Map("age" -> List("wrong")), Map.empty), HNil) === RouteExtractor.BadRequestE("query 'age' has not type Int")
      ext0(EndpointRequest("GET", List("foo"), Map("wrong" -> List("0")), Map.empty), HNil) === RouteExtractor.BadRequestE("missing query 'age'")
      ext0(EndpointRequest("GET", List("foo"), Map.empty, Map.empty), HNil) === RouteExtractor.BadRequestE("missing query 'age'")
      ext0(EndpointRequest("GET", List("foo", "bar"), Map.empty, Map.empty), HNil) === RouteExtractor.NotFoundE

      val ext1 = extract(:= :> "foo" :> Query[Option[Int]]('age) :> Get[Json, Foo])

      ext1(EndpointRequest("GET", List("foo"), Map("age" -> List("0")), Map.empty), HNil) === Right(Some(0) :: HNil)
      ext1(EndpointRequest("GET", List("foo"), Map("wrong" -> List("0")), Map.empty), HNil) === Right(None :: HNil)

      val ext2 = extract(:= :> "foo" :> Query[List[Int]]('age) :> Get[Json, Foo])

      ext2(EndpointRequest("GET", List("foo"), Map("age" -> List("0", "1")), Map.empty), HNil) === Right(List(0, 1) :: HNil)
      ext2(EndpointRequest("GET", List("foo"), Map.empty, Map.empty), HNil) === Right(Nil :: HNil)
    }

    "headers" >> {
      val ext0 = extract(:= :> "foo" :> Header[Int]('age) :> Get[Json, Foo])

      ext0(EndpointRequest("GET", List("foo"), Map.empty, Map("age" -> "0")), HNil) === Right(0 :: HNil)
      ext0(EndpointRequest("GET", List("foo"), Map.empty, Map("age" -> "wrong")), HNil) === RouteExtractor.BadRequestE("header 'age' has not type Int")
      ext0(EndpointRequest("GET", List("foo"), Map.empty, Map("wrong" -> "0")), HNil) === RouteExtractor.BadRequestE("missing header 'age'")
      ext0(EndpointRequest("GET", List("foo"), Map.empty, Map.empty), HNil) === RouteExtractor.BadRequestE("missing header 'age'")
      ext0(EndpointRequest("GET", List("foo", "bar"), Map.empty, Map.empty), HNil) === RouteExtractor.NotFoundE

      val ext2 = extract(:= :> "foo" :> Header[Option[Int]]('age) :> Get[Json, Foo])

      ext2(EndpointRequest("GET", List("foo"), Map.empty, Map("age" -> "0")), HNil) === Right(Some(0) :: HNil)
      ext2(EndpointRequest("GET", List("foo"), Map.empty, Map.empty), HNil) === Right(None :: HNil)

      val ext3 = extract(:= :> "foo" :> Header("Accept", "*") :> Get[Json, Foo])

      ext3(EndpointRequest("GET", List("foo"), Map.empty, Map("accept" -> "*")), HNil) === Right(HNil)
      ext3(EndpointRequest("GET", List("foo"), Map.empty, Map("wrong" -> "*")), HNil) === RouteExtractor.BadRequestE("missing header 'Accept'")
      ext3(EndpointRequest("GET", List("foo"), Map.empty, Map("accept" -> "wrong")), HNil) === RouteExtractor.BadRequestE("header 'Accept' has unexpected value 'wrong' - expected '*'")

      val ext4 = extract(:= :> "foo" :> Server.Send("Accept", "*") :> Get[Json, Foo])

      ext4(EndpointRequest("GET", List("foo"), Map.empty, Map.empty), HNil) === Right(HNil)

      val ext6 = extract(:= :> "foo" :> Server.Match[Int]("age") :> Get[Json, Foo])

      ext6(EndpointRequest("GET", List("foo"), Map.empty, Map("age" -> "0")), HNil) === Right(Map("age" ->0) :: HNil)
      ext6(EndpointRequest("GET", List("foo"), Map.empty, Map("age-and-what-not" -> "0")), HNil) === Right(Map("age-and-what-not" -> 0) :: HNil)
      ext6(EndpointRequest("GET", List("foo"), Map.empty, Map("nope" -> "0")), HNil) === Right(Map.empty :: HNil)
      ext6(EndpointRequest("GET", List("foo"), Map.empty, Map("age-" -> "hello")), HNil) === RouteExtractor.BadRequestE("header 'age-' has not type Int")
    }

    "body type" >> {
      val ext0 = extract(:= :> ReqBody[Json, Foo] :> Put[Json, Foo])

      ext0(EndpointRequest("PUT", Nil, Map.empty, Map.empty), HNil) === Right((BodyType[Foo], HNil))
      ext0(EndpointRequest("PUT", List("foo", "bar"), Map.empty, Map.empty), HNil) === RouteExtractor.NotFoundE
      ext0(EndpointRequest("OPTIONS", List("foo", "bar"), Map.empty, Map.empty), HNil) === RouteExtractor.NotFoundE

      val ext1 = extract(:= :> ReqBody[Json, Foo] :> Post[Json, Foo])

      ext1(EndpointRequest("POST", Nil, Map.empty, Map.empty), HNil) === Right((BodyType[Foo], HNil))
      ext1(EndpointRequest("OPTIONS", Nil, Map.empty, Map.empty), HNil) === Right((BodyType[Foo], HNil))
    }

    "methods" >> {
      val ext0 = extract(:= :> Get[Json, Foo])

      ext0(EndpointRequest("GET", Nil, Map.empty, Map.empty), HNil) === Right(HNil)
      ext0(EndpointRequest("WRONG", Nil, Map.empty, Map.empty), HNil) === RouteExtractor.NotFoundE
      ext0(EndpointRequest("GET", List("foo"), Map.empty, Map.empty), HNil) === RouteExtractor.NotFoundE
      ext0(EndpointRequest("OPTIONS", Nil, Map.empty, Map.empty), HNil) === Right(HNil)

      val ext1 = extract(:= :> Put[Json, Foo])

      ext1(EndpointRequest("PUT", Nil, Map.empty, Map.empty), HNil) === Right(HNil)
      ext1(EndpointRequest("OPTIONS", Nil, Map.empty, Map.empty), HNil) === Right(HNil)

      val ext2 = extract(:= :> Post[Json, Foo])

      ext2(EndpointRequest("POST", Nil, Map.empty, Map.empty), HNil) === Right(HNil)
      ext2(EndpointRequest("OPTIONS", Nil, Map.empty, Map.empty), HNil) === Right(HNil)

      val ext3 = extract(:= :> Delete[Json, Foo])

      ext3(EndpointRequest("DELETE", Nil, Map.empty, Map.empty), HNil) === Right(HNil)
      ext3(EndpointRequest("OPTIONS", Nil, Map.empty, Map.empty), HNil) === Right(HNil)
    }

    "combinations" >> {
      val ext0 = extract(:= :> "foo" :> Query[Int]('age) :> Header[String]('id) :> Get[Json, Foo])

      ext0(EndpointRequest("GET", List("foo"), Map("age" -> List("0")), Map("id" -> "john")), HNil) === Right(0 :: "john" :: HNil)

      val ext1 = extract(:= :> Get[Json, Foo])

      ext1(EndpointRequest("GET", Nil, Map.empty, Map.empty), HNil) === Right(HNil)
    }
  }
}
