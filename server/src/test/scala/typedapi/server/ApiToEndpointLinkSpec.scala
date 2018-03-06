package typedapi.server

import org.specs2.mutable.Specification

final class EndpointCompilationSpec extends Specification {

  case class Foo(name: String)

  "link api definitions to endpoint functions" >> { 
    "single API to endpoint" >> {
      val Api = := :> "find" :> Segment[String]('name) :> Query[Int]('limit) :> Get[List[Foo]]
      val api = transform(Api)
      
      val endpoint0 = typedapi.server.link(api) := { (name, limit) => List(Foo(name)).take(limit) }
      endpoint0.f("john", 10) === List(Foo("john"))

      val endpoint1 = typedapi.server.link(api).to((name, limit) => List(Foo(name)).take(limit))
      endpoint1.f("john", 10) === List(Foo("john"))
    }

    "multiple APIs to multiple endpoints" >> {
      val Api = 
        (:= :> "find" :> Segment[String]('name) :> Get[List[Foo]]) :|:
        (:= :> "create" :> ReqBody[Foo] :> Post[Foo])

      println(Api)

      pending
    }
  }
}
