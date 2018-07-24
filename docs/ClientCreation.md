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

### ScalaJS
If you want to compile to [ScalaJS](https://www.scala-js.org/) you have to use the [Ajax](https://github.com/scala-js/scala-js-dom/blob/master/src/main/scala/org/scalajs/dom/ext/Extensions.scala#L253) with:

```Scala
import typedapi.client.js._
import org.scalajs.dom.ext.Ajax

val cm = ClientManager(Ajax, "http://my-host", myPort)
```

Be aware that `typedapi.client.js` provides a `Encoder[F[_], A]` and `Decoder[F[_], A]` trait to marshall and unmarhsall bodies. You have to provide implementations for your types.

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
