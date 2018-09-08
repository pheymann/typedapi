## Create a server from your API
After we [defined](https://github.com/pheymann/typedapi/blob/master/docs/ApiDefinition.md) our API we have to derive the endpoint/set of endpoints we can mount and serve to the world.

```Scala
val Api =
  (api(Get[Json, User], Root / "user" / Segment[String]("name"))) :|:
  (apiWithBody(Put[Json, User], ReqBody[Json, User], Root / "user"))
```

### First things first, derive the endpoints
```Scala
import typedapi.server._

final case class User(name: String)

// implicit encoders and decoders

val endpoints = deriveAll[IO](Api).from(
  name => // retrieve and return user
  user => // store user
)
```

### Set Status Codes
#### Success
```Scala
deriveAll[IO](Api).from(
  name =>
      val user: User = ???
      
      success(user) // creates a 200
  ...
}
```

or

```Scala
deriveAll[IO](Api).from(
  name =>
      val user: User = ???
      
      successWith(StatusCodes.Ok)(user) // set code
  ...
}
```

#### Error
```Scala
deriveAll[IO](Api).from(
  name =>
      val user: Option[User] = ???
      
      user.fold(errorWith(StatusCodes.NotFound, s"no user $id")(user => success(user))
  ...
}
```

### Http4s
If you want to use [http4s](https://github.com/http4s/http4s) as your server backend you have to add the following code:

```Scala
import typedapi.server.http4s._
import org.http4s.server.blaze.BlazeBuilder

val sm = ServerManager(BlazeBuilder[IO], "http://my-host", myPort)
```

### Akka-Http
If you want to use [akka-http](https://github.com/akka/akka-http) as your server backend you have to add the following code:

```Scala
implicit val timeout = 5.second
implicit val system  = ActorSystem("akka-http-server")
implicit val mat     = ActorMaterializer()

import system.dispatcher
    
val sm = ServerManager(Http(), "http://my-host", myPort)
```

### Start server
Now we can mount `endpoints` and serve to to the world:

```Scala
val server = mount(sm, endpoints)

server.unsafeRunSync()
```

**Make sure** you have the proper encoders and decoders in place.
