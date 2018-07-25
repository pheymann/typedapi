## How to define an API
The central idea behind Typedapi is to make client and server implementation as boilerplate-free, typesafe and simple as possible.

 - On the client-side you only define what you expect from an API provided by a server. In other words, you define a contract between the client and the server.
 - The server-side then has to comply with that contract by implementing proper endpoint functions.
 
But how do you create this API definitions/contracts? This document will show you two ways provided by Typedapi:
  - use the DSL (`import typedapi.dsl._`)
  - or function-call-like definition (`import typedapi._`)
 
### Base case
Every API has to fullfil the base case, meaning it has to have a root path and a method description:
 
```Scala
// dsl
:= :> Get[Json, A]

// function
api(Get[Json, A])
// or
api(method = Get[Json, A], path = Root)
```
 
This translates to `GET /` returning some `Json A`.

### Methods
So far Typedapi supports the following methods:
 
```Scala
// dsl
:= :> Get[Json, A]
:= :> Put[Json, A]
:= :> Post[Json, A]
:= :> Delete[Json, A]

// function
api(Get[Json, A])
api(Put[Json, A])
api(Post[Json, A])
api(Delete[Json, A])
```
 
### Request Body
You may noticed that `Put` and `Post` don't have a field to describe a request body. To add that you have to explicitly define it with an element in your Api:
 
```Scala
// PUT {body: User} /
// dsl
:= :> ReqBody[Json, B] :> Put[Json, A]

// function
apiWithBody(Put[Json, A], ReqBody[Json, B])

// POST {body: User} /
// dsl
:= :> ReqBody[Json, B] :> Post[Json, A]

// function
apiWithBody(Post[Json, A], ReqBody[Json, B])
```
 
By the way, you can only add `Put` and `Post` as the next element of `ReqBody`. Everything else will not compile. Thus, you end up with a valid API description and not something like `:= :> ReqBody[Json, B] :> Get[Json, A]` or `api(Get[Json, A], ReqBody[Json, B])`.

### One word to encodings
You can find a list of provided encodings [here](https://github.com/pheymann/typedapi/blob/update-docs-final-cleanups/shared/src/main/scala/typedapi/shared/ApiElement.scala#L62). If you need something else implement `trait MediaType`.

### Path
```Scala
// GET /hello/world
// dsl
:= :> "hello" :> "world" :> Get[Json, A]

// function
api(Get[Json, A], Root / "hello" / "world")
```
 
All path elements are translated to singleton types and therefore encoded in the type of the API.
 
### Segment
Have a dynamic path element:
 
```Scala
// GET /user/{name: String}
// dsl
:= :> "user" :> Segment[String]("name") :> Get[Json, A]

// function
api(Get[Json, A], Root / "user" / Segment[String]("name"))
```

Every segment gets a name which is again encoded as singleton type in the API type.

### Query Parameter
```Scala
// GET /query?{id: Int}
// dsl
:= :> "query" :> Query[Int]("id") :> Get[Json, A]

// function
api(Get[Json, A], Root / "query", Queries.add[Int]("id"))
```

Every query gets a name which is again encoded as singleton type in the API type.

#### Optional Query
```Scala
// GET /query/opt?{id: Option[Int]}
// dsl
:= :> "query" :> "opt" :> Query[Option[Int]]("id") :> Get[Json, A]

// function
api(Get[Json, A], Root / "query" / "opt", Queries.add[Option[Int]]("id"))
```

#### Query with a List of elements
```Scala
// GET /query/list?{id: List[Int]}
// dsl
:= :> "query" :> "list" :> Query[List[Int]]("id") :> Get[Json, A]

// function
api(Get[Json, A], Root / "query" / "list", Queries.add[List[Int]]("id"))
```

### Header
```Scala
// GET /header {headers: id: Int}
// dsl
:= :> "header" :> Header[Int]("id") :> Get[Json, A]

// function
api(Get[Json, A], Root / "header", headers = Headers.add[Int]("id"))
```

This header is an expected input parameter.

Every header gets a name which is again encoded as singleton type in the API type.

#### Optional Header
```Scala
// GET /header/opt {headers: id: Option[Int]}
// dsl
:= :> "header" :> "opt" :> Header[Option[Int]]("id") :> Get[Json, A]

// function
api(Get[Json, A], Root / "header" / "opt", headers = Headers.add[Option[Int]]("id"))
```

#### Fixed Headers aka static headers
If you have a set of headers which are statically known and have to be provided by all sides you can add them as follows:

```Scala
// GET /header/fixed {headers: consumer=me}
// dsl
:= :> "header" :> "fixed" :> Header("consumer", "me") :> Get[Json, A]

// function
api(Get[Json, A], Root / "header" / "fixed", headers = Headers.add("consumer", "me"))
```

### Multiple definitions in a single API
You can put multiple definitions into a single API element:

```Scala
val Api =
  (:= :> "hello" :> Get[Json, A]) :|:
  (:= :> "world" :> Query[Int]('foo) :> Delete[B])
```
