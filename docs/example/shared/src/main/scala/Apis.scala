
object FromDsl {

  import typedapi.dsl._

  /* NOTE: we have to add the 'Access-Control-Allow-Origin' header to the server-side to allow the 
   * browser (ScalaJS) to access the server (CORS)
   */

  val MyApi = 
    // basic GET request
    (:= :> Server.Send("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    // basic PUT request
    (:= :> Server.Send("Access-Control-Allow-Origin", "*") :> Put[Json, User]) :|:
    // basic POST request
    (:= :> Server.Send("Access-Control-Allow-Origin", "*") :> Post[Json, User]) :|:
    // basic DELETE request
    (:= :> Server.Send("Access-Control-Allow-Origin", "*") :> Delete[Json, User]) :|:
    // define a path
    (:= :> "my" :> "path" :> Server.Send("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    // add a request body
    (:= :> "with" :> "body" :> Server.Send("Access-Control-Allow-Origin", "*") :> ReqBody[Json, User] :> Put[Json, User]) :|:
    // add segments
    (:= :> "name" :> Segment[String]("name") :> Server.Send("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    // add query
    (:= :> "search" :> "user" :> Query[String]("name") :> Server.Send("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    // add header
    (:= :> "header" :> Header[String]("consumer") :> Server.Send("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    (:= :> "header" :> "fixed" :> Header("consumer", "me") :> Server.Send("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    (:= :> "header" :> "client" :> Client.Header("client", "me") :> Server.Send("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    (:= :> "header" :> "client" :> "coll" :> Client.Coll[Int] :> Server.Send("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    (:= :> "header" :> "server" :> Server.Match[String]("Control-") :> Server.Send("Access-Control-Allow-Origin", "*") :> Get[Json, User])
}

object FromDefinition {

  import typedapi._

  val MyApi =
    // basic GET request
    api(Get[Json, User], headers = Headers.serverSend("Access-Control-Allow-Origin", "*")) :|:
    // basic PUT request
    api(Put[Json, User], headers = Headers.serverSend("Access-Control-Allow-Origin", "*")) :|:
    // basic POST request
    api(Post[Json, User], headers = Headers.serverSend("Access-Control-Allow-Origin", "*")) :|:
    // basic Delete request
    api(Delete[Json, User], headers = Headers.serverSend("Access-Control-Allow-Origin", "*")) :|:
    // define a path
    api(Get[Json, User], Root / "my" / "path", headers = Headers.serverSend("Access-Control-Allow-Origin", "*")) :|:
    // add a request body
    apiWithBody(Put[Json, User], ReqBody[Json, User], Root / "with" / "body", headers = Headers.serverSend("Access-Control-Allow-Origin", "*")) :|:
    // add segments
    api(Get[Json, User], Root / "name" / Segment[String]("name"), headers = Headers.serverSend("Access-Control-Allow-Origin", "*")) :|:
    // add query
    api(Get[Json, User], Root / "search" / "user", Queries.add[String]("name"), Headers.serverSend("Access-Control-Allow-Origin", "*")) :|:
    // add header
    api(Get[Json, User], Root / "header", headers = Headers.add[String]("consumer").serverSend("Access-Control-Allow-Origin", "*")) :|:
    api(Get[Json, User], Root / "header" / "fixed", headers = Headers.add("consumer", "me").serverSend("Access-Control-Allow-Origin", "*")) :|:
    api(Get[Json, User], Root / "header" / "client", headers = Headers.client("client", "me").serverSend("Access-Control-Allow-Origin", "*")) :|:
    api(Get[Json, User], Root / "header" / "client" / "coll", headers = Headers.clientColl[Int].serverSend("Access-Control-Allow-Origin", "*")) :|:
    api(Get[Json, User], Root / "header" / "server", headers = Headers.serverMatch[String]("Control-").serverSend("Access-Control-Allow-Origin", "*"))
}
