package typedapi.server

import shapeless._

import org.specs2.mutable.Specification

final class EndpointCompilationSpec extends Specification {

  case class Foo(name: String)

  "link api definitions to endpoint functions" >> { 
    "single API to endpoint" >> {
      val Api = := :> "find" :> Segment[String]('name) :> Query[Int]('limit) :> Get[List[Foo]]

      val endpoint1 = typedapi.server.link(Api).to[Id]((name, limit) => List(Foo(name)).take(limit))
      endpoint1("john" :: 10 :: HNil) === List(Foo("john"))
    }
  }
}
