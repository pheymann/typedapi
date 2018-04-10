## How to define an API
The central idea behind Typedapi is to make client and server implementation as typesafe and simple as possible.

 - On the client-side you only define what you expect from an API provided by a server. In other words you define a contract between the client and the server.
 - The server-side then has to comply with that contract by implementing proper endpoint functions.
 
But how do you create this API definitions/contracts? This document will show you two ways provided by Typedapi:
  - use the DSL (`import typedapi.dsl._`)
  - or function-call-like definition (`import typedapi._`)
 
### Base case
Every API has to fullfil the base case, meaning it has to have a root path and a method description:
 
```Scala
// dsl
:= :> Get[A]

// function
api(Get[A])
// or
api(method = Get[A], path = Root)
```
 
This translates to `GET /` returning some `A`.

### Methods
So far Typedapi supports the following methods:
 
```Scala
// dsl
:= :> Get[A]
:= :> Put[A]
:= :> Post[A]
:= :> Delete[A]

// function
api(Get[A])
api(Put[A])
api(Post[A])
api(Delete[A])
```
 
### Request Body
You may noticed that `Put` and `Post` don't have a field to describe a request body. To add that you have to explicitly define it with an element in your Api:
 
```Scala
// dsl
:= :> ReqBody[B] :> Put[A]
// PUT {body: A} /

// function
apiWithBody(Put[A], ReqBody[B])
// or
apiWithBody(method = Put[A], body = ReqBody[B])
```
 
By the way, you can only add `Put` and `Post` as the next element of `ReqBody`. Everything else will not compile. Thus, you end up with a valid API description and not something like `:= :> ReqBody[B] :> Get[A]` or `api(Get[A], ReqBody[B])`.
 
### Path
When you want to describe more than just the root path you can add path elements:
 
```Scala
// dsl
:= :> "hello" :> "world" :> Get[A]
// GET /hello/world

:= :> "hello" :> "world" :> ReqBody[B] :> Put[A]
// PUT {body: B} /hello/world

// function
api(Get[A], Root / "hello" / "world")
apiWithBody(Put[A], ReqBody[B], Root / "hello" / "world")
```
 
All path elements are translated to singleton types and therefore encoded in the type of the API.
 
### Segment
You can also put information into the path by using segments:
 
```Scala
// dsl
:= :> Segment[Int]('id) :> Get[A]
// GET /{id}

:= :> "hello" :> Segment[String]('name) :> Get[A]
// GET /hello/{name}

:= :> "hello" :> Segment[String]('name) :> ReqBody[B]:> Put[A]
// PUT {body: B} /hello/{name}

// function
api(Get[A], Root / Segment[Int]('id))
api(Get[A], Root / "hello" / Segment[String]('name)
apiWithBody(Put[A], ReqBody[B], Root / "hello" / Segment[String]('name))
```

Every segment gets a name which is again encoded as singleton type in the API type. **You have** to use `Symbol`s for names.

### Query Parameter
You can add query parameters with:

```Scala
// dsl
:= :> "hello" :> Query[Int]('id) :> Get[A]
// GET /hello?id={: Int}

:= :> "hello" :> Query[Int]('id) :> ReqBody[B] :> Put[A]
// PUT {body: B} /hello?id={: Int}

// function
api(Get[A], Root / "hello", Queries.add(Query[Int]('id)))
apiWithBody(Put[A], ReqBody[B], Root / "hello", Queries.add(Query[Int]('id)))
```

Every query gets a name which is again encoded as singleton type in the API type. **You have** to use `Symbol`s for names.

#### Optional Query
```Scala
// dsl
:= :> "hello" :> Query[Option[Int]]('id) :> Get[A]

// function
api(Get[A], Root / "hello", Queries.add(Query[Option[Int]]('id)))
```

#### Query with a List of elements
```Scala
// dsl
:= :> "hello" :> Query[List[Int]]('id) :> Get[A]

// function
api(Get[A], Root / "hello", Queries.add(Query[List[Int]]('id)))
```

### Header
You can add header parameters with:

```Scala
// dsl
:= :> "hello" :> Header[Int]('id) :> Get[A]
// GET /hello {headers: id={:Int}}

:= :> "hello" :> Header[Int]('id) :> ReqBody[B] :> Put[A]
// PUT {body: B} /hello {headers: id={:Int}}

:= :> "hello" :> Query[String]('name) :> Header[Int]('id) :> Get[A]
// GET /hello?name={:String} {headers: id={:Int}}

// function
api(Get[A], Root / "hello", headers = Headers.add(Header[Int]('id)))
apiWithBody(Put[A], ReqBody[B], Root / "hello", headers = Headers.add(Header[Int]('id)))
api(Get[A], Root / "hello", Queries.add(Query[String]('name)), Headers.add(Header[Int]('id)))
```

Every header gets a name which is again encoded as singleton type in the API type. **You have** to use `Symbol`s for names.

#### Optional Header
```Scala
// dsl
:= :> "hello" :> Header[Option[Int]]('id) :> Get[A]

// function
api(Get[A], Root / "hello", headers = Headers.add(Header[Option[Int]]('id)))
```

#### Add multiple headers at once
Sometimes you have to pass a set of standard headers with every request, but you don't want to encode them in every API. Typedapi provides a convinience element calles `RawHeaders` which is a `Map[String, String]`.

```Scala
// dsl
:= :> "hello" :> RawHeaders :> Get[A]
// GET /hello {headers: any pair of String -> String}

// function
api(Get[A], Root / "hello", headers = Headers.add(RawHeaders))
```

You cannot define a typesafe header after a `RawHeaders` element. Furthermore, you should use this with care as it is not typesafe.

### Multiple definitions in a single API
You can put multiple definitions into a single API element:

```Scala
val Api =
  (:= :> "hello" :> Get[A]) :|:
  (:= :> "world" :> Query[Int]('foo) :> Delete[B])
```
