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
        val t0   = transform(:= :> Get[ReqInput])
        val api0 = compile(t0)
        api0().run[Id] === ReqInput("GET", Nil, Map(), Map())

        val t1   = transform(:= :> Put[ReqInput])
        val api1 = compile(t1)
        api1().run[Id] === ReqInput("PUT", Nil, Map(), Map())

        val t2   = transform(:= :> Post[ReqInput])
        val api2 = compile(t2)
        api2().run[Id] === ReqInput("POST", Nil, Map(), Map())

        val t3   = transform(:= :> Delete[ReqInput])
        val api3 = compile(t3)
        api3().run[Id] === ReqInput("DELETE", Nil, Map(), Map())
      }
      
      "segment" >> {
        val t0   = transform(:= :> Segment[Int]('i0) :> Get[ReqInput])
        val api0 = compile(t0)
        api0(0).run[Id] === ReqInput("GET", "0" :: Nil, Map(), Map())

        val t1   = transform(:= :> Segment[Int]('i0) :> Segment[Int]('i1) :> Get[ReqInput])
        val api1 = compile(t1)
        api1(0, 1).run[Id] === ReqInput("GET", "0" :: "1" :: Nil, Map(), Map())
      }

      "query" >> {
        val t0   = transform(:= :> Query[Int]('i0) :> Get[ReqInput])
        val api0 = compile(t0)
        api0(0).run[Id] === ReqInput("GET", Nil, Map("i0" -> List("0")), Map())

        val t1   = transform(:= :> Query[Int]('i0) :> Query[Int]('i1) :> Get[ReqInput])
        val api1 = compile(t1)
        api1(0, 1).run[Id] === ReqInput("GET", Nil, Map("i0" -> List("0"), "i1" -> List("1")), Map())
      }

      "header" >> {
        val t0   = transform(:= :> Header[Int]('i0) :> Get[ReqInput])
        val api0 = compile(t0)
        api0(0).run[Id] === ReqInput("GET", Nil, Map(), Map("i0" -> "0"))

        val t1   = transform(:= :> Header[Int]('i0) :> Header[Int]('i1) :> Get[ReqInput])
        val api1 = compile(t1)
        api1(0, 1).run[Id] === ReqInput("GET", Nil, Map(), Map("i0" -> "0", "i1" -> "1"))
      }

      "raw header" >> {
        val t0   = transform(:= :> RawHeaders :> Get[ReqInput])
        val api0 = compile(t0)
        api0(Map("i0" -> "0")).run[Id] === ReqInput("GET", Nil, Map(), Map("i0" -> "0"))
      }

      "request body" >> {
        val t0   = transform(:= :> ReqBody[Int] :> Put[ReqInputWithBody[Int]])
        val api0 = compile(t0)
        api0(0).run[Id] === ReqInputWithBody("PUT", Nil, Map(), Map(), 0)
      }

      "path" >> {
        val t0   = transform(:= :> "hello" :> "world" :> Get[ReqInput])
        val api0 = compile(t0)
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
