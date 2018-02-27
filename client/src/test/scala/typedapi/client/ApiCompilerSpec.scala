package typedapi.client

import typedapi.client.test._

import shapeless.Id
import org.specs2.mutable.Specification

final class ApiCompilerSpec extends Specification {

  case class Foo()

  type Result = (String, List[String], Map[String, String], Map[String, String], Option[Foo])

  implicit val get       = testGet[Id, ReqInput](identity)
  implicit val put       = testPut[Id, ReqInput](identity)
  implicit def putB[Bd]  = testPutWithBody[Id, Bd, ReqInputWithBody[Bd]](identity)
  implicit val post      = testPost[Id, ReqInput](identity)
  implicit def postB[Bd] = testPostWithBody[Id, Bd, ReqInputWithBody[Bd]](identity)
  implicit val delete    = testDelete[Id, ReqInput](identity)

  "executes compiled api" >> {
    implicit val cm = clientManager

    "single api" >> {
      "method" >> {
        val api0 = compile(transform(:= :> Get[ReqInput]))
        api0().run[Id] === ReqInput("GET", Nil, Map(), Map())
        val api1 = compile(transform(:= :> Put[ReqInput]))
        api1().run[Id] === ReqInput("PUT", Nil, Map(), Map())
        val api2 = compile(transform(:= :> Post[ReqInput]))
        api2().run[Id] === ReqInput("POST", Nil, Map(), Map())
        val api3 = compile(transform(:= :> Delete[ReqInput]))
        api3().run[Id] === ReqInput("DELETE", Nil, Map(), Map())
      }

      "segment" >> {
        val api0 = compile(transform(:= :> Segment[Int]('i0) :> Get[ReqInput]))
        api0(0).run[Id] === ReqInput("GET", "0" :: Nil, Map(), Map())
        val api1 = compile(transform(:= :> Segment[Int]('i0) :> Segment[Int]('i1) :> Get[ReqInput]))
        api1(0, 1).run[Id] === ReqInput("GET", "0" :: "1" :: Nil, Map(), Map())
      }

      "query" >> {
        val api0 = compile(transform(:= :> Query[Int]('i0) :> Get[ReqInput]))
        api0(0).run[Id] === ReqInput("GET", Nil, Map("i0" -> List("0")), Map())
        val api1 = compile(transform(:= :> Query[Int]('i0) :> Query[Int]('i1) :> Get[ReqInput]))
        api1(0, 1).run[Id] === ReqInput("GET", Nil, Map("i0" -> List("0"), "i1" -> List("1")), Map())
      }

      "header" >> {
        val api0 = compile(transform(:= :> Header[Int]('i0) :> Get[ReqInput]))
        api0(0).run[Id] === ReqInput("GET", Nil, Map(), Map("i0" -> "0"))
        val api1 = compile(transform(:= :> Header[Int]('i0) :> Header[Int]('i1) :> Get[ReqInput]))
        api1(0, 1).run[Id] === ReqInput("GET", Nil, Map(), Map("i0" -> "0", "i1" -> "1"))
      }

      "raw header" >> {
        val api0 = compile(transform(:= :> RawHeaders :> Get[ReqInput]))
        api0(Map("i0" -> "0")).run[Id] === ReqInput("GET", Nil, Map(), Map("i0" -> "0"))
      }

      "request body" >> {
        val api0 = compile(transform(:= :> ReqBody[Int] :> Put[ReqInputWithBody[Int]]))
        api0(0).run[Id] === ReqInputWithBody("PUT", Nil, Map(), Map(), 0)
      }

      "path" >> {
        val api0 = compile(transform(:= :> "hello" :> "world" :> Get[ReqInput]))
        api0().run[Id] === ReqInput("GET", "hello" :: "world" :: Nil, Map(), Map())
      }
    }

    "composition" >> {
      val api = 
        (:= :> "find" :> Get[ReqInput]) :|:
        (:= :> "fetch" :> Segment[String]('type) :> Get[ReqInput]) :|:
        (:= :> "store" :> ReqBody[Int] :> Post[ReqInputWithBody[Int]])

      val (find :|: fetch :|: store :|: =:) = compile(transform(api))

      find().run[Id] === ReqInput("GET", "find" :: Nil, Map(), Map())
      fetch("all").run[Id] === ReqInput("GET", "fetch" :: "all" :: Nil, Map(), Map())
      store(0).run[Id] === ReqInputWithBody("POST", "store" :: Nil, Map(), Map(), 0)
    }
  }
}
