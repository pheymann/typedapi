### 0.1.0-RC4 / Towards a stable API
 - changes to the client API:
 ```Scala
 val Api     = := :> Get[Foo]
 val ApiList =
   (:= :> Get[Foo]) :|:
   (:= :> RequestBody[Foo] :> Put[Foo])
 
 // not compile, but
 val foo = derive(Api)
 
 val (foo2 :|: bar :|: =:) = deriveAll(ApiList)
 
 ...
 // explicitly pass ClientManager
 foo().run[IO](cm)
 _______________^
 ```
 
 - changes to the server API
 ```Scala
 // not link to, but
 val endpoint = derive[IO](Api).from(...)
 
 val endpoints = deriveAll[IO](ApiList).from(...)
 ```
 
 - major changes were applied to the internal code to reach a stable state (see [this PR](https://github.com/pheymann/typedapi/pull/13))
