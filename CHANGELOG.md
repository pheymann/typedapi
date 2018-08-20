### 0.2.0
 - internal: separated decoded and raw requests with `RawApiRequest` and `ApiRequest`
 - fixed `implicitNotFound` message for `ApiRequest`
 - implemented change suggested in #31 (ClientManager Enhancements)

### 0.1.0
 - internal cleanups and refactorings
 - extended example project and added ScalaJS client
 - centralized http-support specs
 - added akka-http support on server and client-side
 - added scalaj-http support on the client-side
 - added ScalaJS compilation support for shared and client code
 - implemented basic ScalaJS client
 - added body encoding types and made them mandatory (several hundred Mediatypes supported)
   ```Scala
   := :> ReqBody[Json, User] :> Get[Json, User]
    _______________^__________________^
   ```
 
 - `RawHeaders` was removed
 - fixed headers were added; a fixed header is a statically known key-value pair, therefore, no input is required
   ```Scala
   // dsl
   := :> Header("Access-Control-Allow-Origin", "*") :> Get[Json, User]
   
   // function
   api(Get[Json, User], headers = Headers add("Access-Control-Allow-Origin", "*"))
   ```
   
 - changes to the server API:
   - `NoReqBodyExecutor` and `ReqBodyExecutor` now expect a `MethodType`:
   ```Scala
   new NoReqBodyExecutor[El, KIn, VIn, M, F, FOut] {
   ____________________________________^
  
   new ReqBodyExecutor[El, KIn, VIn, Bd, M, ROut, POut, F, FOut] {
   ______________________________________^
   ```
   
   - fixed header only sent by the server
   ```Scala
   := :> Server.Send("Access-Control-Allow-Origin", "*") :> Get[Json, User]
   
   api(Get[Json, User], Headers.serverSend("Access-Control-Allow-Origin", "*"))
   ```
   - extract headers which have keys that match a `String`
   ```Scala
   := :> Server.Match[String]("Control") :> Get[Json, User]
   
   api(Get[Json, User], Headers.serverMatch[String]("Control"))
   ```
 - changes to the client API:
   - new encoding types add `Content-Type` and `Accept` headers
   - fixed header only sent by the client
   ```Scala
   := :> Client.Header("Access-Control-Allow-Origin", "*") :> Get[Json, User]
   
   api(Get[Json, User], Headers.client("Access-Control-Allow-Origin", "*"))
   ```
   - send dynamic header ignore it on the server-side
   ```Scala
   := :> Client.Header[String]("Foo") :> Get[Json, User]
   
   api(Get[Json, User], Headers.client[String]("Foo"))
   ```

### 0.1.0-RC5 / Almost there
 - changes to the client API:
 ```Scala
 val ApiList =
   (:= :> Get[Foo]) :|:
   (:= :> RequestBody[Foo] :> Put[Foo])
   
 // `:|:` removed for API compositions
 val (get, put) = deriveAll(ApiList)
 ```
 
 - changes to the server API:
 ```Scala
 // same for endpoint compositions
 val e = deriveAll[IO](ApiList).from(get, put)
 ```

### 0.1.0-RC4 / Towards a stable API
 - changes to the client API:
 ```Scala
 val Api     = := :> Get[Foo]
 val ApiList =
   (:= :> Get[Foo]) :|:
   (:= :> RequestBody[Foo] :> Put[Foo])
 
 // not `compile`, but
 val foo = derive(Api)
 
 val (foo2 :|: bar :|: =:) = deriveAll(ApiList)
 
 ...
 // explicitly pass ClientManager
 foo().run[IO](cm)
 _______________^
 ```
 
 - changes to the server API
 ```Scala
 // not `link.to`, but
 val endpoint = derive[IO](Api).from(...)
 
 val endpoints = deriveAll[IO](ApiList).from(...)
 ```
 
 - major changes were applied to the internal code to reach a stable state (see [this PR](https://github.com/pheymann/typedapi/pull/13))
