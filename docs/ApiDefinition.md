## How to define an API
The central idea behind TypedApi is to make client and server implementation as typesafe and simple as possible.

 - On the client-side you only define what you expect from an API provided by a server. In other words you define a contract between the client and the server.
 - The server-side then has to comply with that contract by implementing proper endpoint functions.
 
But how do you create this API definitions/contracts?
 
### Base case
Every API has to fullfil the base case, meaning it has to have a root and a method description:
 
```Scala
val Api = := :> Get[A]
```
 
This translates to `GET /` returning some `A`.

### Methods
So far TypedApi supports the following methods:
 
```Scala
:= :> Get[A]
:= :> Put[A]
:= :> Post[A]
:= :> Delete[A]
```
 
### Request Body
You may noticed that `Put` and `Post` don't have a field to describe a request body. To add that you have to explicitly define it with an element in your Api:
 
```Scala
:= :> ReqBody[B] :> Put[A]
// PUT {body: A} /
```
 
By the way, you can only add `Put` and `Post` as the next element of `ReqBody`. Everything else will not compile. Thus, you end up with a valid API description and not something like `:= :> ReqBody[B] :> Get[A]`.
 
### Path
When you want to describe more than just the root path you can add path elements:
 
```Scala
:= :> "hello" :> "world" :> Get[A]
// GET /hello/world

:= :> "hello" :> "world" :> ReqBody[B] :> Put[A]
// PUT {body: B} /hello/world
```
 
All path elements are translated to singleton types and therefore encoded in the type of the API.
 
### Segment
You can also put information into the path by using segments:
 
```Scala
:= :> Segment[Int]('id) :> Get[A]
// GET /{id}

:= :> "hello" :> Segment[String]('name) :> Get[A]
// GET /hello/{name}

:= :> "hello" :> Segment[String]('name) :> ReqBody[B]:> Put[A]
// PUT {body: B} /hello/{name}
```

Every segment gets a name which is again encoded as singleton type in the API type. **You have** to use `Symbol`s for names.

### Query Parameter
You can add query parameters with:

```Scala
:= :> "hello" :> Query[Int]('id) :> Get[A]
// GET /hello?id={: Int}

:= :> "hello" :> Query[Int]('id) :> ReqBody[B] :> Put[A]
// PUT {body: B} /hello?id={: Int}
```

Every query gets a name which is again encoded as singleton type in the API type. **You have** to use `Symbol`s for names.

### Header
You can add header parameters with:

```Scala
:= :> "hello" :> Header[Int]('id) :> Get[A]
// GET /hello {headers: id={:Int}}

:= :> "hello" :> Header[Int]('id) :> ReqBody[B] :> Put[A]
// PUT {body: B} /hello {headers: id={:Int}}

:= :> "hello" :> Query[String]('name) :> Header[Int]('id) :> Get[A]
// GET /hello?name={:String} {headers: id={:Int}}
```

Every header gets a name which is again encoded as singleton type in the API type. **You have** to use `Symbol`s for names.

#### Add multiple headers at once
Sometimes you have to pass a set of standard headers with every request, but you don't want to encode them in every API. TypedApi provides a convinience element calles `RawHeaders` which is a `Map[String, String]`.

```Scala
:= :> "hello" :> RawHeaders :> Get[A]
// GET /hello {headers: any pair of String -> String}
```

You cannot define a typesafe header after a `RawHeaders` element. Furthermore, you should use this with care as it is not typesafe.

### Multiple definitions in a single API
You can put multiple definitions into a single API element:

```Scala
val Api =
  (:= :> "hello" :> Get[A]) :|:
  (:= :> "world" :> Query[Int]('foo) :> Delete[B])
```
