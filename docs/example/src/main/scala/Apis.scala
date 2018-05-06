
object FromDsl {

  import typedapi.dsl._

  val MyApi = 
    // GET /fetch/user?name=<>
    (:= :> "fetch" :> "user" :> Query[String]('name) :> Get[User]) :|:
    // POST /create/user
    (:= :> "create" :> "user" :> ReqBody[User] :> Post[User])
}

object FromDefinition {

  import typedapi._

  val MyApi =
    // GET /fetch/user?name=<>
    api(method = Get[User], path = Root / "fetch" / "user", queries = Queries add Query[String]('name)) :|:
    // POST /create/user
    apiWithBody(method = Post[User], body = ReqBody[User], path = Root / "create" / "user")
}
