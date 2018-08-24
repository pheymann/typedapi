package http.support

import typedapi.dsl._

package object tests {

  val Api =
    (:= :> "path" :> Get[Json, User]) :|:
    (:= :> "segment" :> Segment[String]('name) :> Get[Json, User]) :|:
    (:= :> "query" :> Query[Int]('age) :> Get[Json, User]) :|:
    (:= :> "header" :> Header[Int]('age) :> Get[Json, User]) :|:
    (:= :> "header" :> "fixed" :> Header("Hello", "*") :> Get[Json, User]) :|:
    (:= :> "header" :> "input" :> "client" :> Client.Header[String]("Hello") :> Get[Json, User]) :|:
    (:= :> "header" :> "client" :> Client.Header("Hello", "*") :> Get[Json, User]) :|:
    (:= :> "header" :> "server" :> "match" :> Server.Match[String]("test") :> Get[Json, User]) :|:
    (:= :> "header" :> "server" :> "send" :> Server.Send("Hello", "*") :> Get[Json, User]) :|:
    (:= :> Get[Json, User]) :|:
    (:= :> Put[Json, User]) :|:
    (:= :> "body" :> ReqBody[Json, User] :> Put[Json, User]) :|:
    (:= :> Post[Json, User]) :|:
    (:= :> "body" :> ReqBody[Json, User] :> Post[Json, User]) :|:
    (:= :> Query[List[String]]('reasons) :> Delete[Json, User]) :|:
    (:= :> "status" :> "200" :> Get[Plain, String]) :|:
    (:= :> "status" :> "400" :> Get[Plain, String]) :|:
    (:= :> "status" :> "500" :> Get[Plain, String])
}
