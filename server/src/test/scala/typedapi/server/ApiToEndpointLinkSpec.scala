package typedapi.server

import typedapi.dsl._
import shapeless._

import org.specs2.mutable.Specification

final class ApiToEndpointLinkSpec extends Specification {

  case class Foo(name: String)

  "link api definitions to endpoint functions" >> { 
    val Api = := :> "find" :> typedapi.dsl.Segment[String]('name) :> Query[Int]('limit) :> Get[List[Foo]]

    val endpoint0 = typedapi.server.link(Api).to[Id]((name, limit) => List(Foo(name)).take(limit))
    endpoint0("john" :: 10 :: HNil) === List(Foo("john"))

    val endpoint1 = typedapi.server.link(Api).to((name, limit) => Option(List(Foo(name)).take(limit)))
    endpoint1("john" :: 10 :: HNil) === Some(List(Foo("john")))
  }
}
