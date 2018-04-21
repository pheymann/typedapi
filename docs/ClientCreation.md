## Create a client from your API
After we [defined](https://github.com/pheymann/typedapi/blob/master/docs/ApiDefinition.md) our API we have to derive a function/set of functions we can use to make our calls.

### First thing first, derive your functions
Lets derive our functions:

```Scala
import typedapi.client._

final case class User(name: String)

val Api = := :> "my" :> "awesome" :> "api" :> Segment[String]('name) :> Get[User]

val myAwesomeApi = derive(Api)
```

### Http4s
If you want to use [http4s](https://github.com/http4s/http4s) as your client backend you have to add the following code:

```Scala
import typedapi.client.http4s._
import cats.effect.IO
import org.http4s.client.blaze.Http1Client

val client = Http1Client[IO]().unsafeRunSync
val cm     = ClientManager(client, "http://my-host", myPort)
```

Now we can to use `myAwesomeApi`:

```Scala
val r = myAwesomeApi("Joe").run[IO](cm)
//r: IO[User]
```

**Make sure** you have the proper `Encoder`s and `Decoder`s in place.

#### Multiple APIs
```Scala
import typedapi.client._
import typedapi.client.http4s._
import cats.effect.IO
import org.http4s.client.blaze.Http1Client

val Api = 
  (:= :> "user" :> Segment[String]('name) :> Get[User]) :|:
  (:= :> "user" :> ReqBody[User] :> Put[User])

val (find :|: create :|: =:) = deriveAll(Api)

val client = Http1Client[IO]().unsafeRunSync
val cm     = ClientManager(client, "http://my-host", myPort)

val r0 = create(User("Joe")).run[IO](cm)
//r0: IO[User]

val r1 = find("Joe").run[IO](cm)
//r1: IO[User]
```
