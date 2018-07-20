
object FromDsl {

  import typedapi.dsl._

  val MyApi = 
    // GET /fetch/user?name=<>
    (:= :> "fetch" :> "user" :> Query[String]('name) :> Server("Access-Control-Allow-Origin", "*") :> Get[Json, User]) :|:
    // POST /create/user
    (:= :> "create" :> "user" :> Server("Access-Control-Allow-Origin", "*") :> ReqBody[Json, User] :> Post[Json, User])
}

object FromDefinition {

  import typedapi._

  val MyApi =
    // GET /fetch/user?name=<>
    api(
      method = Get[Json, User], 
      path = Root / "fetch" / "user", 
      queries = Queries add[String]('name),
      headers = Headers.server("Access-Control-Allow-Origin", "*")
    ) :|:
    // POST /create/user
    apiWithBody(
      method = Post[Json, User], 
      body = ReqBody[Json, User], 
      path = Root / "create" / "user",
      headers = Headers.server("Access-Control-Allow-Origin", "*")
    )
}
