package http.support

import typedapi.dsl._

package object tests {

  val Api =
    (:= :> "path" :> Get[User]) :|:
    (:= :> "segment" :> Segment[String]('name) :> Get[User]) :|:
    (:= :> "query" :> Query[Int]('age) :> Get[User]) :|:
    (:= :> "header" :> Header[Int]('age) :> Get[User]) :|:
    (:= :> "header" :> "raw" :> Header[Int]('age) :> RawHeaders :> Get[User]) :|:
    (:= :> Get[User]) :|:
    (:= :> Put[User]) :|:
    (:= :> "body" :> ReqBody[User] :> Put[User]) :|:
    (:= :> Post[User]) :|:
    (:= :> "body" :> ReqBody[User] :> Post[User]) :|:
    (:= :> Query[List[String]]('reasons) :> Delete[User])
}
