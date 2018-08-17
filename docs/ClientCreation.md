## Create a client from your API
After we [defined](https://github.com/pheymann/typedapi/blob/master/docs/ApiDefinition.md) our API we have to derive a function/set of functions we can use to make our calls.

```Scala
val Api =
  (api(Get[Json, User], Root / "user" / Segment[String]("name"))) :|:
  (apiWithBody(Put[Json, User], ReqBody[Json, User], Root / "user"))
```

### First thing first, derive your functions
Lets derive our functions:

```Scala
import typedapi.client._

final case class User(name: String)

// implicit encoders and decoders

val (get, create) = deriveAll(Api)
```

### Http4s
If you want to use [http4s](https://github.com/http4s/http4s) as your client backend you have to add the following code:

```Scala
import typedapi.client.http4s._
import org.http4s.client.blaze.Http1Client

val client = Http1Client[IO]().unsafeRunSync
val cm     = ClientManager(client, "http://my-host", myPort)
```

### Akka-Http
If you want to use [akka-http](https://github.com/akka/akka-http) as your client backend you have to add the following code:

```Scala
import typedapi.client.akkahttp._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http

implicit val timeout = 5.second
implicit val system  = ActorSystem("akka-http-client")
implicit val mat     = ActorMaterializer()

import system.dispatcher

val cm = ClientManager(Http(), "http://my-host", myPort)
```

### Scalaj-Http
If you want to use [scalaj-http](https://github.com/scalaj/scalaj-http) as your client backend you have to add the following code:

```Scala
import typedapi.client.scalajhttp._
import scalaj.http._

val cm = ClientManager(Http, "http://my-host", myPort)
```
Be aware that `typedapi.util` provides an `Encoder[F[_], A]` and `Decoder[F[_], A]` trait to marshall and unmarhsall bodies. You have to provide implementations for your types.

```Scala
implicit val decoder = Decoder[Future, User] { json =>
  // unmarshall the json using some known lib like circe
}

implicit val encoder = Encoder[Future, User] { user =>
  // marshall the user using some known lib like circe
}
```

### Ammonite
There is special support for [Ammonite](http://ammonite.io/#Ammonite-REPL) and [ScalaScripts](http://ammonite.io/#ScalaScripts). It lets you tinker with the raw response and reduces the amount of imports you have to do:

```Scala
import $ivy.`com.github.pheymann::typedapi-ammonite-client:<version>`

import typedapi._
import client._
import amm._

val cm = clientManager("http://localhost", 9000)

final case class User(name: String, age: Int)

val Api = api(Get[Json, User], Root / "user" / "url")

val get = derive(Api)

// gives you the raw scalaj-http response
val response = get().run[Id].raw(cm)

response.body
response.headers
...
```

No `Decoder` needed if you use `raw(cm)`. Under the covers it uses scalaj-http as a client library.

It can be, that Ammonite isn't able to load `com.dwijnand:sbt-compat:1.0.0`. If that is the case execute the following command:

```Scala
interp.repositories() ++= Seq(coursier.ivy.IvyRepository.fromPattern(
  "https://dl.bintray.com/dwijnand/sbt-plugins/" +:
  coursier.ivy.Pattern.default
))
```

### ScalaJS
If you want to compile to [ScalaJS](https://www.scala-js.org/) you have to use the [Ajax](https://github.com/scala-js/scala-js-dom/blob/master/src/main/scala/org/scalajs/dom/ext/Extensions.scala#L253) with:

```Scala
import typedapi.client.js._
import org.scalajs.dom.ext.Ajax

val cm = ClientManager(Ajax, "http://my-host", myPort)
```

Be aware that `typedapi.util` provides an `Encoder[F[_], A]` and `Decoder[F[_], A]` trait to marshall and unmarhsall bodies. You have to provide implementations for your types.

```Scala
implicit val decoder = Decoder[Future, User] { json =>
  // unmarshall the json using some known lib like circe
}

implicit val encoder = Encoder[Future, User] { user =>
  // marshall the user using some known lib like circe
}
```

### Usage
Now we can to use our client functions:

```Scala
for {
  _    <- create(User("Joe", 42)).run[IO](cm)
  user <- get("Joe").run[IO](cm)
} yield user

//F[User]
```

**Make sure** you have the proper encoders and decoders in place.
