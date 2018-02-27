# Typedapi
Define type safe APIs and let the Scala compiler do the rest:

```Scala
import typedapi.client._

val MyApi = 
  (:= :> "fetch" :> "user" :> Query[String]('sortBy) :> Get[List[User]]) :|:
  (:= :> "create" :> "user" :> ReqBody[User] :> Post[Unit])

val (fetch :|: create :|: :=) = compile(transform(MyApi))

import typedapi.client.http4s._

fetch("age").run[IO]: IO[List[User]]
```

This is all you have to do to define an API with multiple endpoints and to create call functions for them. No extra code is needed.

## Motivation
This library is the result of the following questions:

> How much can we encode on the type level? Are we able to describe a whole API and generate the call functions from that without using Macros?

It is inspired by [Servant](https://github.com/haskell-servant/servant) and right now a WIP. It provides an API layer which is client agnostic and currently supports:

  - [http4s](https://github.com/http4s/http4s)

Support is planned for:
  - API for server side
  - [akka-http](https://github.com/akka/akka-http)

## Get this library
It is available for Scala 2.11 and 2.12 and can be install as local build:

```
git clone https://github.com/pheymann/typedapi.git
cd typedapi
sbt "+ publishLocal"
```

A Maven artifact will be available soon.

## How to use it
### Basic structure
An API endpoint always starts with `:= :>` followed by any number of `ApiElement`s. Every `ApiElement` can be used as initial element.

#### Path
```Scala
// /find/user
val api = := :> "find" :> "user"
```

Can be followed by:
 - `Path`
 - `Segment`
 - `Query`
 - `Header`
 - `ReqBody`
 - a method: `Get`, ...

#### Segment
```Scala
// /find/{name: String}
val api = := :> "find" :> Segment[String]('name) ...
```
 
Can be followed by:
 - `Path`
 - `Segment`
 - `Query`
 - `Header`
 - `ReqBody`
 - a method: `Get`, ...

#### Query
```Scala
// /find?name={value: String}
val api = := :> "find" :> Query[String]('name)

// /find?name={value0, value1, ...}
val api = := :> "find" :> Query[List[String]]('name)
```
Can be followed by:
 - `Query`
 - `Header`
 - `ReqBody`
 - a method: `Get`, ...

#### Header
```Scala
// /find - Header: name={value: String}
val api = := :> "find" :> Header[String]('name)

// /find - Headers: Map[String, String]
// UNSAFE convenience input, expects a `rawHeaders: Map[String, String]`
val api = := :> "find" :> RawHeaders
```
Can be followed by:
 - `Header`
 - `ReqBody`
 - a method: `Get`, ...

#### ReqBody
```Scala
// /find - `body: Foo`
val api = := :> "find" :> ReqBody[Foo]
```
Can be followed by:
 - a method: `Get`, ...

#### Method
```Scala
val api0 = := :> "find" :> Get[Foo]
val api1 = := :> "find" :> Put[Foo]
val api2 = := :> "find" :> Post[Foo]
val api3 = := :> "find" :> Delete[Foo]
```

### Compile and run executable
```
val Api = := :> "fetch" :> Segment[String]('name) :> Get[User]
val api = compile(transform(api))
```
You can now use `api` to run requests as long as a client implementation of [ApiRequest](https://github.com/pheymann/typedapi/client/src/main/scala/typedapi/client/ApiRequest.scala) is in scope.

#### Functional call style
```Scala
api("John").run[IO]
```

#### Case Class call style
If field names of your `case class` do not align with extracted definition from your API input elements (`Segment`, `Query`, ...) the compiler will raise a compilation error.

```Scala
case class Find(name: String)

api(Find("John")).run[IO]

case class FindWrong(nome: String)

// will not compile
api(FindWrong("John")).run[IO]
```

### Compose multiple endpoints to a single API
```Scala
val api = 
  (:= :> "user" :> Segment[String]('name) :> Get[User]) :|:
  (:= :> "user" :> ReBody[User] :> Post[Unit]) :|:
  (:= :> "user" :> Segment[String]('name) :> Delete[Unit])

val (find :|: create :|: delete :|: =:) = compile(transform(api))

find("John").run[IO]
```

## Add client support
To enable **typedapi** to use a client library not supported yet you have to implement a (small) API called [ApiRequest](https://github.com/pheymann/typedapi/client/src/main/scala/typedapi/client/ApiRequest.scala). Take a look at [http4s](https://github.com/http4s/http4s) for an example.

## Dependencies
 - [shapeless 2.3.3](https://github.com/milessabin/shapeless/)

## Contribution
Contributions are highly welcome. If you find a bug or you are missing the support for a specific client library consider opening a PR with your solution.
