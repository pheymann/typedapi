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
