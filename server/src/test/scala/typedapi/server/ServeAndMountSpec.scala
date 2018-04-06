package typedapi.server

import typedapi.dsl._
import shapeless.{HList, HNil, Id, ::}
import shapeless.ops.hlist.Prepend
import org.specs2.mutable.Specification

import scala.language.higherKinds

final class ServeAndMountSpec extends Specification {

  case class Foo(name: String)

  sealed trait Req
  case class TestRequest(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String]) extends Req
  case class TestRequestWithBody[Bd](uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd) extends Req

  case class TestResponse(raw: String)

  implicit def execNoBodyId[El <: HList, In <: HList, CIn <: HList, FOut] = 
    new NoReqBodyExecutor[El, In, CIn, Id, FOut] {
      type R = Req
      type Out = TestResponse

      def apply(req: Req, eReq: EndpointRequest, endpoint: Endpoint[El, In, CIn, CIn, Id, FOut]): Either[ExtractionError, Out] =
        extract(eReq, endpoint).right.map { extracted => 
          TestResponse(execute(extracted, endpoint).toString())
        }
    }

  implicit def execWithBody[El <: HList, In <: HList, Bd , ROut <: HList, POut <: HList, CIn <: HList, FOut](implicit _prepend: Prepend.Aux[ROut, Bd :: HNil, POut], _eqProof: POut =:= CIn) = 
    new ReqBodyExecutor[El, In, Bd, ROut, POut, CIn, Id, FOut] {
      type R = Req
      type Out = TestResponse

      implicit val prepend = _prepend
      implicit def eqProof = _eqProof

      def apply(req: Req, eReq: EndpointRequest, endpoint: Endpoint[El, In, (BodyType[Bd], ROut), CIn, Id, FOut]): Either[ExtractionError, Out] =
        extract(eReq, endpoint).right.map { case (_, extracted) => 
          TestResponse(execute(extracted, req.asInstanceOf[TestRequestWithBody[Bd]].body, endpoint).toString())
        }
    }

  def toList[El <: HList, In <: HList, ROut, CIn <: HList, F[_], FOut](endpoint: Endpoint[El, In, ROut, CIn, F, FOut])
                                                                      (implicit executor: EndpointExecutor[El, In, ROut, CIn, F, FOut]): List[Serve[executor.R, executor.Out]] = 
    List(new Serve[executor.R, executor.Out] {
      def apply(req: executor.R, eReq: EndpointRequest): Either[ExtractionError, executor.Out] = executor(req, eReq, endpoint)
    })

  def toList[End <: HList](end: End)(implicit s: ServeToList[End, Req, TestResponse]): List[Serve[Req, TestResponse]] =
    s(end)

  "serve endpoints as simple Request -> Response functions and mount them into a server" >> {
    "serve single endpoint and no body" >> {
      val Api      = := :> "find" :> "user" :> Segment[String]('name) :> Query[Int]('sortByAge) :> Get[List[Foo]]
      val endpoint = typedapi.server.link(Api).to[Id]((name, sortByAge) => List(Foo(name)))
      val served   = toList(endpoint)

      val req  = TestRequest(List("find", "user", "joe"), Map("sortByAge" -> List("1")), Map.empty)
      val eReq = EndpointRequest("GET", req.uri, req.queries, req.headers)

      served.head(req, eReq) === Right(TestResponse("List(Foo(joe))"))
    }

    "serve single endpoint and with body" >> {
      val Api      = := :> "find" :> "user" :> Segment[String]('name) :> ReqBody[Foo] :> Post[List[Foo]]
      val endpoint = typedapi.server.link(Api).to[Id]((name, body) => List(Foo(name), body))
      val served   = toList(endpoint)

      val req  = TestRequestWithBody(List("find", "user", "joe"), Map.empty, Map.empty, Foo("jim"))
      val eReq = EndpointRequest("POST", req.uri, req.queries, req.headers)

      served.head(req, eReq) === Right(TestResponse("List(Foo(joe), Foo(jim))"))
    }

    "serve multiple endpoints" >> {
      val Api = 
        (:= :> "find" :> "user" :> Segment[String]('name) :> Query[Int]('sortByAge) :> Get[List[Foo]]) :|:
        (:= :> "create" :> "user" :> ReqBody[Foo] :> Post[Foo])

      def find(name: String, age: Int): Id[List[Foo]] = List(Foo(name))
      def create(foo: Foo): Id[Foo] = foo

      val endpoints = typedapi.server.link(Api).to[Id] {
        find _ :|:
        create _ :|: =:
      }

      val served = toList(endpoints)

      val req  = TestRequest(List("find", "user", "joe"), Map("sortByAge" -> List("1")), Map.empty)
      val eReq = EndpointRequest("GET", req.uri, req.queries, req.headers)

      served.head(req, eReq) === Right(TestResponse("List(Foo(joe))"))
    }
  }
}
