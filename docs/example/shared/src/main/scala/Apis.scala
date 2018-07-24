
object FromDsl {

  import typedapi.dsl._

  /* NOTE: we have to add the 'Access-Control-Allow-Origin' header to the server-side to allow the 
   * browser (ScalaJS) to access the server (CORS)
   */

  val MyApi = 
    // basic GET request
    (:= :> Server("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    // basic PUT request
    (:= :> Server("Access-Control-Allow-Origin", "*") :> Put[Json, User]) :|:
    // basic POST request
    (:= :> Server("Access-Control-Allow-Origin", "*") :> Post[Json, User]) :|:
    // basic DELETE request
    (:= :> Server("Access-Control-Allow-Origin", "*") :> Delete[Json, User]) :|:
    // define a path
    (:= :> "my" :> "path" :> Server("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    // add a request body
    (:= :> "with" :> "body" :> Server("Access-Control-Allow-Origin", "*") :> ReqBody[Json, User] :> Put[Json, User]) :|:
    // add segments
    (:= :> "name" :> Segment[String]("name") :> Server("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    // add query
    (:= :> "search" :> "user" :> Query[String]("name") :> Server("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    // add header
    (:= :> "header" :> Header[String]("consumer") :> Server("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    (:= :> "header" :> "fixed" :> Fixed("consumer", "me") :> Server("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    (:= :> "header" :> "client" :> Client("client", "me") :> Server("Access-Control-Allow-Origin", "*") :> Get[Json, User])
}

object FromDefinition {

  import typedapi._

  val MyApi =
    // basic GET request
    api(Get[Json, User], headers = Headers.server("Access-Control-Allow-Origin", "*")) :|:
    // basic PUT request
    api(Put[Json, User], headers = Headers.server("Access-Control-Allow-Origin", "*")) :|:
    // basic POST request
    api(Post[Json, User], headers = Headers.server("Access-Control-Allow-Origin", "*")) :|:
    // basic Delete request
    api(Delete[Json, User], headers = Headers.server("Access-Control-Allow-Origin", "*")) :|:
    // define a path
    api(Get[Json, User], Root / "my" / "path", headers = Headers.server("Access-Control-Allow-Origin", "*")) :|:
    // add a request body
    apiWithBody(Put[Json, User], ReqBody[Json, User], Root / "with" / "body", headers = Headers.server("Access-Control-Allow-Origin", "*")) :|:
    // add segments
    api(Get[Json, User], Root / "name" / Segment[String]("name"), headers = Headers.server("Access-Control-Allow-Origin", "*")) :|:
    // add query
    api(Get[Json, User], Root / "search" / "user", Queries.add[String]("name"), Headers.server("Access-Control-Allow-Origin", "*")) :|:
    // add header
    api(Get[Json, User], Root / "header", headers = Headers.add[String]("consumer").server("Access-Control-Allow-Origin", "*")) :|:
    api(Get[Json, User], Root / "header" / "fixed", headers = Headers.add("consumer", "me").server("Access-Control-Allow-Origin", "*")) :|:
    api(Get[Json, User], Root / "header", headers = Headers.client("client", "me").server("Access-Control-Allow-Origin", "*"))
}
