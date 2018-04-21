## Create a server from your API
After we [defined](https://github.com/pheymann/typedapi/blob/master/docs/ApiDefinition.md) our API we have to derive the endpoint/set of endpoints we can mount and serve to the world.

### First things first, derive the endpoints
```Scala
import typedapi.server._

final case class User(name: String)

val Api = := :> "my" :> "awesome" :> "api" :> Segment[String]('name) :> Get[User]

val endpoint = derive[IO](Api).from(name => ???)
```

### Http4s
If you want to use [http4s](https://github.com/http4s/http4s) as your server backend you have to add the following code:

```Scala
import typedapi.server.http4s._
import cats.effect.IO
import org.http4s.server.blaze.BlazeBuilder

val builder = BlazeBuilder[IO]
val cm      = ServerManager(builder, "http://my-host", myPort)
```

Now we can mount `endpoint` and serve to to the world:

```Scala
val server = mount(sm, endpoint)

server.unsafeRunSync()
```

**Make sure** you have the proper `Encoder`s and `Decoder`s in place.

#### Multiple APIs
```Scala
import typedapi.server._
import typedapi.server.http4s._
import cats.effect.IO
import org.http4s.server.blaze.BlazeBuilder

val Api = 
  (:= :> "user" :> Segment[String]('name) :> Get[User]) :|:
  (:= :> "user" :> ReqBody[User] :> Put[User])

def find(name: String): IO[User] = ???
def create(user: User): IO[User] = ???

val endpoints = deriveAll[IO](Api).from(find _ :|: create _ :|: =:)

val builder = BlazeBuilder[IO]
val cm      = ServerManager(builder, "http://my-host", myPort)

val server = mount(sm, endpoints)

server.unsafeRunSync()
```
