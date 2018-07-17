package typedapi.server

import typedapi.dsl._
import shapeless._

import org.specs2.mutable.Specification

final class ApiToEndpointLinkSpec extends Specification {

  case class Foo(name: String)

  "link api definitions to endpoint functions" >> { 
    val Api = := :> "find" :> typedapi.dsl.Segment[String]('name) :> Query[Int]('limit) :> Get[Json, List[Foo]]

    val endpoint0 = derive[Option].apply(Api).from((name, limit) => Some(List(Foo(name)).take(limit)))
    endpoint0("john" :: 10 :: HNil) === Some(List(Foo("john")))

    val endpoint1 = derive[Option](Api).from((name, limit) => Option(List(Foo(name)).take(limit)))
    endpoint1("john" :: 10 :: HNil) === Some(List(Foo("john")))
  }
}
