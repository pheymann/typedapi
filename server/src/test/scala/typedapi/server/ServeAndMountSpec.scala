package typedapi.server

import typedapi.dsl._
import typedapi.shared.MethodType
import shapeless.{HList, HNil, ::}
import shapeless.ops.hlist.{Prepend, Mapper}
import org.specs2.mutable.Specification

import scala.language.higherKinds

final class ServeAndMountSpec extends Specification {

  import StatusCodes._

  case class Foo(name: String)

  sealed trait Req
  case class TestRequest(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String]) extends Req
  case class TestRequestWithBody[Bd](uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd) extends Req

  case class TestResponse(raw: Any)

  implicit def execNoBodyId[El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, FOut] = 
    new NoReqBodyExecutor[El, KIn, VIn, M, Option, FOut] {
      type R = Req
      type Out = TestResponse

      def apply(req: Req, eReq: EndpointRequest, endpoint: Endpoint[El, KIn, VIn, M, VIn, Option, FOut]): Either[ExtractionError, Out] =
        extract(eReq, endpoint).right.map { extracted => 
          TestResponse(execute(extracted, endpoint))
        }
    }

  implicit def execWithBody[El <: HList, KIn <: HList, VIn <: HList, Bd, M <: MethodType, ROut <: HList, POut <: HList, FOut](implicit _prepend: Prepend.Aux[ROut, Bd :: HNil, POut], _eqProof: POut =:= VIn) = 
    new ReqBodyExecutor[El, KIn, VIn, Bd, M, ROut, POut, Option, FOut] {
      type R = Req
      type Out = TestResponse

      implicit val prepend = _prepend
      implicit def eqProof = _eqProof

      def apply(req: Req, eReq: EndpointRequest, endpoint: Endpoint[El, KIn, VIn, M, (BodyType[Bd], ROut), Option, FOut]): Either[ExtractionError, Out] =
        extract(eReq, endpoint).right.map { case (_, extracted) => 
          TestResponse(execute(extracted, req.asInstanceOf[TestRequestWithBody[Bd]].body, endpoint))
        }
    }

  def toList[El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, ROut, F[_], FOut](endpoint: Endpoint[El, KIn, VIn, M, ROut, F, FOut])
                                                                                        (implicit executor: EndpointExecutor[El, KIn, VIn, M, ROut, F, FOut]): List[Serve[executor.R, executor.Out]] = 
    List(new Serve[executor.R, executor.Out] {
      def options(eReq: EndpointRequest): Option[(String, Map[String, String])] = {
        endpoint.extractor(eReq, HNil) match {
          case Right(_) => Some((endpoint.method, endpoint.headers))
          case _        => None
        }
      }

      def apply(req: executor.R, eReq: EndpointRequest): Either[ExtractionError, executor.Out] = executor(req, eReq, endpoint)
    })

  def toList[End <: HList, Serv <: HList](end: End)(implicit mapper: Mapper.Aux[endpointToServe.type, End, Serv], s: ServeToList[Serv, Req, TestResponse]): List[Serve[Req, TestResponse]] =
    s(end.map(endpointToServe))

  "serve endpoints as simple Request -> Response functions and mount them into a server" >> {
    "serve single endpoint and no body" >> {
      val Api      = := :> "find" :> "user" :> Segment[String]('name) :> Query[Int]('sortByAge) :> Get[Json, List[Foo]]
      val endpoint = derive[Option](Api).from((name, sortByAge) => Some(successWith(Ok)(List(Foo(name)))))
      val served   = toList(endpoint)

      val req  = TestRequest(List("find", "user", "joe"), Map("sortByAge" -> List("1")), Map.empty)
      val eReq = EndpointRequest("GET", req.uri, req.queries, req.headers)

      served.head(req, eReq) === Right(TestResponse(Some(Right(Ok -> List(Foo("joe"))))))
    }

    "check if route exists and return method" >> {
      val Api      = := :> "find" :> "user" :> Segment[String]('name) :> Query[Int]('sortByAge) :> Server.Send("Hello", "*") :> Get[Json, List[Foo]]
      val endpoint = derive[Option](Api).from((name, sortByAge) => Some(successWith(Ok)(List(Foo(name)))))
      val served   = toList(endpoint)

      val req0  = TestRequest(List("find", "user", "joe"), Map("sortByAge" -> List("1")), Map.empty)
      val eReq0 = EndpointRequest("GET", req0.uri, req0.queries, req0.headers)

      served.head.options(eReq0) === Some(("GET", Map(("Hello", "*"))))

      val eReq1 = EndpointRequest("POST", req0.uri, req0.queries, req0.headers)

      served.head.options(eReq1) === None
    }

    "serve single endpoint and with body" >> {
      val Api      = := :> "find" :> "user" :> Segment[String]('name) :> ReqBody[Json, Foo] :> Post[Json, List[Foo]]
      val endpoint = derive[Option](Api).from((name, body) => Some(successWith(Ok)(List(Foo(name), body))))
      val served   = toList(endpoint)

      val req  = TestRequestWithBody(List("find", "user", "joe"), Map.empty, Map.empty, Foo("jim"))
      val eReq = EndpointRequest("POST", req.uri, req.queries, req.headers)

      served.head(req, eReq) === Right(TestResponse(Some(Right(Ok -> List(Foo("joe"), Foo("jim"))))))
    }

    "serve multiple endpoints" >> {
      val Api = 
        (:= :> "find" :> "user" :> Segment[String]('name) :> Query[Int]('sortByAge) :> Get[Json, List[Foo]]) :|:
        (:= :> "create" :> "user" :> ReqBody[Json, Foo] :> Post[Json, Foo])

      def find(name: String, age: Int): Option[Result[List[Foo]]] = Some(successWith(Ok)(List(Foo(name))))
      def create(foo: Foo): Option[Result[Foo]] = Some(successWith(Ok)(foo))

      val endpoints = deriveAll[Option](Api).from(find _, create _)

      val served = toList(endpoints)

      val req  = TestRequest(List("find", "user", "joe"), Map("sortByAge" -> List("1")), Map.empty)
      val eReq = EndpointRequest("GET", req.uri, req.queries, req.headers)

      served.head(req, eReq) === Right(TestResponse(Some(Right(Ok -> List(Foo("joe"))))))
    }
  }
}
