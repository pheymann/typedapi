
object FromDsl {

  import typedapi.dsl._

  val MyApi = 
    // GET /fetch/user?name=<>
    (:= :> "fetch" :> "user" :> Query[String]('name) :> Header[String]("Access-Control-Allow-Origin") :> Get[Json, User]) :|:
    // POST /create/user
    (:= :> "create" :> "user" :> Header[String]("Access-Control-Allow-Origin") :> ReqBody[Json, User] :> Post[Json, User])
}

object FromDefinition {

  import typedapi._

  val MyApi =
    // GET /fetch/user?name=<>
    api(
      method = Get[Json, User], 
      path = Root / "fetch" / "user", 
      queries = Queries add Query[String]('name),
      headers = Headers add Header[String]("Access-Control-Allow-Origin")
    ) :|:
    // POST /create/user
    apiWithBody(
      method = Post[Json, User], 
      body = ReqBody[Json, User], 
      path = Root / "create" / "user",
      headers = Headers add Header[String]("Access-Control-Allow-Origin")
    )
}
