package typedapi.server

import typedapi.shared._
import shapeless.{HList, HNil}
import shapeless.ops.hlist.Reverse
import org.specs2.mutable.Specification

final class RouteExtractorSpec extends Specification {

  case class Foo(name: String)

  def extract[H <: HList, El <: HList, REl <: HList, In <: HList, RIn <: HList, R <: EndpointRequest, Out](transformed: TypeLevelFoldLeft.Aux[H, (HNil, HNil), (El, In, Out)])
                                                                                    (implicit revEl: Reverse.Aux[El, REl], revIn: Reverse.Aux[In, RIn], ext: RouteExtractor[R, REl, RIn, HNil]): RouteExtractor[R, REl, RIn, HNil] = 
    ext

  "determine routes defined by requests and extract included data (segments, queries, headers)" >> {
    "no data" >> {
      val api = transform(:= :> "hello" :> "world" :> Get[Foo])

      extract(api).apply(SimpleEndpointRequest("GET", List("hello", "world").reverse, Map.empty, Map.empty), Set.empty, HNil) === Some(HNil)
      extract(api).apply(SimpleEndpointRequest("GET", List("hello", "wrong").reverse, Map.empty, Map.empty), Set.empty, HNil) === None
      extract(api).apply(SimpleEndpointRequest("GET", List("hello"), Map.empty, Map.empty), Set.empty, HNil) === None
      extract(api).apply(SimpleEndpointRequest("GET", Nil, Map.empty, Map.empty), Set.empty, HNil) === None
    }

    "segments" >> {
      val api = transform(:= :> "foo" :> Segment[Int]('age) :> Get[Foo])

      extract(api).apply(SimpleEndpointRequest("GET", List("foo", "0").reverse, Map.empty, Map.empty), Set.empty, HNil) === Some(0 :: HNil)
      extract(api).apply(SimpleEndpointRequest("GET", List("foo", "wrong").reverse, Map.empty, Map.empty), Set.empty, HNil) === None
      extract(api).apply(SimpleEndpointRequest("GET", List("foo"), Map.empty, Map.empty), Set.empty, HNil) === None
      extract(api).apply(SimpleEndpointRequest("GET", Nil, Map.empty, Map.empty), Set.empty, HNil) === None
    }

    "queries" >> {
      val api = transform(:= :> "foo" :> Query[Int]('age) :> Get[Foo])

      extract(api).apply(SimpleEndpointRequest("GET", List("foo"), Map("age" -> "0"), Map.empty), Set.empty, HNil) === Some(0 :: HNil)
      extract(api).apply(SimpleEndpointRequest("GET", List("foo"), Map("age" -> "wrong"), Map.empty), Set.empty, HNil) === None
      extract(api).apply(SimpleEndpointRequest("GET", List("foo"), Map("wrong" -> "0"), Map.empty), Set.empty, HNil) === None
      extract(api).apply(SimpleEndpointRequest("GET", List("foo"), Map.empty, Map.empty), Set.empty, HNil) === None
    }

    "headers" >> {
      val api0 = transform(:= :> "foo" :> Header[Int]('age) :> Get[Foo])

      extract(api0).apply(SimpleEndpointRequest("GET", List("foo"), Map.empty, Map("age" -> "0")), Set.empty, HNil) === Some(0 :: HNil)
      extract(api0).apply(SimpleEndpointRequest("GET", List("foo"), Map.empty, Map("age" -> "wrong")), Set.empty, HNil) === None
      extract(api0).apply(SimpleEndpointRequest("GET", List("foo"), Map.empty, Map("wrong" -> "0")), Set.empty, HNil) === None
      extract(api0).apply(SimpleEndpointRequest("GET", List("foo"), Map.empty, Map.empty), Set.empty, HNil) === None

      //raw
      val api1 = transform(:= :> "foo" :> RawHeaders :> Get[Foo])

      extract(api1).apply(SimpleEndpointRequest("GET", List("foo"), Map.empty, Map("age" -> "blah")), Set.empty, HNil) === Some(Map("age" -> "blah") :: HNil)
      extract(api1).apply(SimpleEndpointRequest("GET", List("foo"), Map.empty, Map.empty), Set.empty, HNil) === None

      // headers and raw
      //val api2 = transform(:= :> "foo" :> Header[Int]('age) :> RawHeaders :> Get[Foo])

      //extract(api2).apply(SimpleEndpointRequest("GET", List("foo"), Map.empty, Map("age" -> "0")), Set.empty, HNil) === None
      //extract(api2).apply(SimpleEndpointRequest("GET", List("foo"), Map.empty, Map("age" -> "0", "foo" -> "bar")), Set.empty, HNil) === Some(0 :: Map("foo" -> "bar") :: HNil)
      pending
    }

    "body type" >> {
      val api0 = transform(:= :> ReqBody[Foo] :> Put[Foo])

      extract(api0).apply(BodyEndpointRequest("PUT", Nil, Map.empty, Map.empty, Foo("john")), Set.empty, HNil) === Some(Foo("john") :: HNil)

      val api1 = transform(:= :> ReqBody[Foo] :> Post[Foo])

      extract(api1).apply(BodyEndpointRequest("POST", Nil, Map.empty, Map.empty, Foo("john")), Set.empty, HNil) === Some(Foo("john") :: HNil)
    }

    "methods" >> {
      val api0 = transform(:= :> Get[Foo])

      extract(api0).apply(SimpleEndpointRequest("GET", Nil, Map.empty, Map.empty), Set.empty, HNil) === Some(HNil)
      extract(api0).apply(SimpleEndpointRequest("WRONG", Nil, Map.empty, Map.empty), Set.empty, HNil) === None

      val api1 = transform(:= :> Put[Foo])

      extract(api1).apply(SimpleEndpointRequest("PUT", Nil, Map.empty, Map.empty), Set.empty, HNil) === Some(HNil)

      val api2 = transform(:= :> Post[Foo])

      extract(api2).apply(SimpleEndpointRequest("POST", Nil, Map.empty, Map.empty), Set.empty, HNil) === Some(HNil)

      val api3 = transform(:= :> Delete[Foo])

      extract(api3).apply(SimpleEndpointRequest("DELETE", Nil, Map.empty, Map.empty), Set.empty, HNil) === Some(HNil)
    }

    "combinations" >> {
      val api0 = transform(:= :> "foo" :> Query[Int]('age) :> Header[String]('id) :> Get[Foo])

      extract(api0).apply(SimpleEndpointRequest("GET", List("foo"), Map("age" -> "0"), Map("id" -> "john")), Set.empty, HNil) === Some(0 :: "john" :: HNil)

      val api1 = transform(:= :> Get[Foo])

      extract(api1).apply(SimpleEndpointRequest("GET", Nil, Map.empty, Map.empty), Set.empty, HNil) === Some(HNil)
    }
  }
}
