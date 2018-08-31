package typedapi.client

import typedapi.dsl._
import typedapi.client.test._

import shapeless.Id
import org.specs2.mutable.Specification

final class RequestDataBuilderSpec extends Specification {

  case class Foo()

  type Result = (String, List[String], Map[String, String], Map[String, String], Option[Foo])

  implicit val get       = testGet[Id, ReqInput](identity)
  implicit val put       = testPut[Id, ReqInput](identity)
  implicit def putB[Bd]  = testPutWithBody[Id, Bd, ReqInputWithBody[Bd]](identity)
  implicit val post      = testPost[Id, ReqInput](identity)
  implicit def postB[Bd] = testPostWithBody[Id, Bd, ReqInputWithBody[Bd]](identity)
  implicit val delete    = testDelete[Id, ReqInput](identity)

  "executes compiled api" >> {
    val cm = clientManager

    "single api" >> {
      "method" >> {
        val api0 = derive(:= :> Get[Json, ReqInput])
        api0().run[Id](cm) === ReqInput("GET", Nil, Map(), Map(("Accept", "application/json")))
        val api1 = derive(:= :> Put[Json, ReqInput])
        api1().run[Id](cm) === ReqInput("PUT", Nil, Map(), Map(("Accept", "application/json")))
        val api2 = derive(:= :> Post[Json, ReqInput])
        api2().run[Id](cm) === ReqInput("POST", Nil, Map(), Map(("Accept", "application/json")))
        val api3 = derive(:= :> Delete[Json, ReqInput])
        api3().run[Id](cm) === ReqInput("DELETE", Nil, Map(), Map(("Accept", "application/json")))
      }
      
      "segment" >> {
        val api0 = derive(:= :> Segment[Int]('i0) :> Get[Json, ReqInput])
        api0(0).run[Id](cm) === ReqInput("GET", "0" :: Nil, Map(), Map(("Accept", "application/json")))
        val api1 = derive(:= :> Segment[Int]('i0) :> Segment[Int]('i1) :> Get[Json, ReqInput])
        api1(0, 1).run[Id](cm) === ReqInput("GET", "0" :: "1" :: Nil, Map(), Map(("Accept", "application/json")))
      }

      "query" >> {
        val api0 = derive(:= :> Query[Int]('i0) :> Get[Json, ReqInput])
        api0(0).run[Id](cm) === ReqInput("GET", Nil, Map("i0" -> List("0")), Map(("Accept", "application/json")))
        val api1 = derive(:= :> Query[Int]('i0) :> Query[Int]('i1) :> Get[Json, ReqInput])
        api1(0, 1).run[Id](cm) === ReqInput("GET", Nil, Map("i0" -> List("0"), "i1" -> List("1")), Map(("Accept", "application/json")))
        val api2 = derive(:= :> Query[List[Int]]('i0) :> Get[Json, ReqInput])
        api2(List(0, 1)).run[Id](cm) === ReqInput("GET", Nil, Map("i0" -> List("0", "1")), Map(("Accept", "application/json")))
        api2(Nil).run[Id](cm) === ReqInput("GET", Nil, Map.empty, Map(("Accept", "application/json")))
        val api3 = derive(:= :> Query[Option[Int]]('i0) :> Get[Json, ReqInput])
        api3(Some(0)).run[Id](cm) === ReqInput("GET", Nil, Map("i0" -> List("0")), Map(("Accept", "application/json")))
        api3(None).run[Id](cm) === ReqInput("GET", Nil, Map.empty, Map(("Accept", "application/json")))
      }

      "header" >> {
        val api0 = derive(:= :> Header[Int]('i0) :> Get[Json, ReqInput])
        api0(0).run[Id](cm) === ReqInput("GET", Nil, Map(), Map("Accept" -> "application/json", "i0" -> "0"))
        val api1 = derive(:= :> Header[Int]('i0) :> Header[Int]('i1) :> Get[Json, ReqInput])
        api1(0, 1).run[Id](cm) === ReqInput("GET", Nil, Map(), Map("Accept" -> "application/json", "i0" -> "0", "i1" -> "1"))
        val api2 = derive(:= :> Header[Option[Int]]('i0) :> Get[Json, ReqInput])
        api2(Some(0)).run[Id](cm) === ReqInput("GET", Nil, Map(), Map("Accept" -> "application/json", "i0" -> "0"))
        api2(None).run[Id](cm) === ReqInput("GET", Nil, Map(), Map("Accept" -> "application/json"))
        val api3 = derive(:= :> Header('i0, 'i1) :> Get[Json, ReqInput])
        api3().run[Id](cm) === ReqInput("GET", Nil, Map(), Map("Accept" -> "application/json", "i0" -> "i1"))
        val api4 = derive(:= :> Client.Header[Int]('i0) :> Get[Json, ReqInput])
        api4(0).run[Id](cm) === ReqInput("GET", Nil, Map(), Map("Accept" -> "application/json", "i0" -> "0"))
        val api5 = derive(:= :> Client.Header('i0, 'i1) :> Get[Json, ReqInput])
        api5().run[Id](cm) === ReqInput("GET", Nil, Map(), Map("Accept" -> "application/json", "i0" -> "i1"))
        val api6 = derive(:= :> Client.Coll[Int] :> Get[Json, ReqInput])
        api6(Map("hello" -> 5)).run[Id](cm) === ReqInput("GET", Nil, Map(), Map("Accept" -> "application/json", "hello" -> "5"))
      }

      "ignore server elements" >> {
        val api0 = derive(:= :> Server.Match[String]("Hello-") :> Server.Send("a", "b") :> Client.Header[Int]('i0) :> Get[Json, ReqInput])
        api0(0).run[Id](cm) === ReqInput("GET", Nil, Map(), Map("Accept" -> "application/json", "i0" -> "0"))
      }

      "request body" >> {
        val api0 = derive(:= :> ReqBody[Json, Int] :> Put[Json, ReqInputWithBody[Int]])
        api0(0).run[Id](cm) === ReqInputWithBody("PUT", Nil, Map(), Map(("Accept", "application/json")), 0)
      }

      "path" >> {
        val api0 = derive(:= :> "hello" :> "world" :> Get[Json, ReqInput])
        api0().run[Id](cm) === ReqInput("GET", "hello" :: "world" :: Nil, Map(), Map(("Accept", "application/json")))
      }
    }

    "raw" >> {
      implicit def rawPutB[Bd] = testRawPutWithBody[Id, Bd](identity)

      val api0 = derive(:= :> ReqBody[Json, Int] :> Put[Json, ReqInputWithBody[Int]])
      api0(0).run[Id].raw(cm) === ReqInputWithBody("PUT", Nil, Map(), Map(("Accept", "application/json")), 0)
    }

    "composition" >> {
      val api = 
        (:= :> "find" :> Server.Match[String]("Hello-") :> Server.Send("a", "b") :> Get[Json, ReqInput]) :|:
        (:= :> "fetch" :> Segment[String]('type) :> Get[Json, ReqInput]) :|:
        (:= :> "store" :> ReqBody[Json, Int] :> Post[Json, ReqInputWithBody[Int]])

      val (find, fetch, store) = deriveAll(api)

      find().run[Id](cm) === ReqInput("GET", "find" :: Nil, Map(), Map(("Accept", "application/json")))
      fetch("all").run[Id](cm) === ReqInput("GET", "fetch" :: "all" :: Nil, Map(), Map(("Accept", "application/json")))
      store(0).run[Id](cm) === ReqInputWithBody("POST", "store" :: Nil, Map(), Map(("Accept", "application/json")), 0)
    }
  }
}
