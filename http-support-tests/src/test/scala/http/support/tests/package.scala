package http.support

import typedapi.dsl._

package object tests {

  val Api =
    (:= :> "path" :> Get[Json, User]) :|:
    (:= :> "segment" :> Segment[String]('name) :> Get[Json, User]) :|:
    (:= :> "query" :> Query[Int]('age) :> Get[Json, User]) :|:
    (:= :> "header" :> Header[Int]('age) :> Get[Json, User]) :|:
    (:= :> "header" :> "fixed" :> Fixed("Hello", "*") :> Get[Json, User]) :|:
    (:= :> Get[Json, User]) :|:
    (:= :> Put[Json, User]) :|:
    (:= :> "body" :> ReqBody[Json, User] :> Put[Json, User]) :|:
    (:= :> Post[Json, User]) :|:
    (:= :> "body" :> ReqBody[Json, User] :> Post[Json, User]) :|:
    (:= :> Query[List[String]]('reasons) :> Delete[Json, User])
}
