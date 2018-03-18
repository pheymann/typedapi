package typedapi.server

import shapeless.{HList, HNil, Id, ::}
import shapeless.ops.hlist.Prepend
import org.specs2.mutable.Specification

final class ServeAndMountSpec extends Specification {

  case class Foo(name: String)

  case class TestRequest(uri: List[String], queries: Map[String, String], headers: Map[String, String])
  case class TestRequestWithBody[Bd](uri: List[String], queries: Map[String, String], headers: Map[String, String], body: Bd)
  case class TestResponse(raw: String)

  implicit def execNoBodyId[El <: HList, In <: HList, CIn <: HList, FOut] = 
    new NoReqBodyExecutor[El, In, CIn, Id, FOut] {
      type R = TestRequest
      type Out = TestResponse

      def apply(req: TestRequest, eReq: EndpointRequest, endpoint: Endpoint[El, In, CIn, CIn, Id, FOut]): Option[Out] =
        extract(eReq, endpoint).map { extracted => 
          TestResponse(execute(extracted, endpoint).toString())
        }
    }

  implicit def execWithBody[El <: HList, In <: HList, Bd , ROut <: HList, POut <: HList, CIn <: HList, FOut](implicit _prepend: Prepend.Aux[ROut, Bd :: HNil, POut], _eqProof: POut =:= CIn) = 
    new ReqBodyExecutor[El, In, Bd, ROut, POut, CIn, Id, FOut] {
      type R = TestRequestWithBody[Bd]
      type Out = TestResponse

      implicit val prepend = _prepend
      implicit def eqProof = _eqProof

      def apply(req: TestRequestWithBody[Bd], eReq: EndpointRequest, endpoint: Endpoint[El, In, (BodyType[Bd], ROut), CIn, Id, FOut]): Option[Out] =
        extract(eReq, endpoint).map { case (_, extracted) => 
          TestResponse(execute(extracted, req.body, endpoint).toString())
        }
    }

  "serve endpoints as simple Request -> Response functions and mount them into a server" >> {
    "serve single endpoint and no body" >> {
      val Api      = := :> "find" :> "user" :> Segment[String]('name) :> Query[Int]('sortByAge) :> Get[List[Foo]]
      val endpoint = typedapi.server.link(Api).to[Id]((name, sortByAge) => List(Foo(name)))
      val served   = serve(endpoint)

      val req  = TestRequest(List("find", "user", "joe"), Map("sortByAge" -> "1"), Map.empty)
      val eReq = EndpointRequest("GET", req.uri, req.queries, req.headers)

      served.head(req, eReq) === Some(TestResponse("List(Foo(joe))"))
    }

    "serve single endpoint and with body" >> {
      val Api      = := :> "find" :> "user" :> Segment[String]('name) :> ReqBody[Foo] :> Post[List[Foo]]
      val endpoint = typedapi.server.link(Api).to[Id]((name, body) => List(Foo(name), body))
      val served   = serve(endpoint)

      val req  = TestRequestWithBody(List("find", "user", "joe"), Map.empty, Map.empty, Foo("jim"))
      val eReq = EndpointRequest("POST", req.uri, req.queries, req.headers)

      served.head(req, eReq) === Some(TestResponse("List(Foo(joe), Foo(jim))"))
    }
  }
}
