## Extend Typedapi to fit your needs
You ended up in this file if:
 - the default implements for a HTTP framework doesn't fit your needs
 - if the framework you want to use is not supported
 - you exceed some of the limitations of this library

### Write your own Client backend
To write your own client backend you have to implement the [ApiRequest](https://github.com/pheymann/typedapi/blob/master/client/src/main/scala/typedapi/client/ApiRequest.scala) type-classes:
  - `GetRequest`
  - `PutRequest` and `PutWithBodyRequest`
  - `PostRequest` and `PostWithBodyRequest`
  - `DeleteRequest`

Take a look into [http4s-client](https://github.com/pheymann/typedapi/blob/master/http4s-client/src/main/scala/typedapi/client/http4s/package.scala) to get an idea how to do it.

You can implement all type-classes or just a subset to override implementations provided by TypedApi.

### Write your own Server backend
To write your own server backend you have to implement the [EndpointExecutor](https://github.com/pheymann/typedapi/blob/master/server/src/main/scala/typedapi/server/EndpointExecutor.scala) and [MountEndpoints](https://github.com/pheymann/typedapi/blob/master/server/src/main/scala/typedapi/server/ServerManager.scala) type-classes

Take a look into [http4s-server](https://github.com/pheymann/typedapi/blob/master/http4s-server/src/main/scala/typedapi/server/http4s/package.scala) to get an idea how to do it.

You can implement all type-classes or just a subset to override implementations provided by TypedApi.

### Define APIs with more than 10 input elements
Right Typedapi is only able to support API definitions with at most 10 input elements. This is because [ApiCompilerOps](https://github.com/pheymann/typedapi/blob/master/client/src/main/scala/typedapi/client/ops/ApiCompilerOps.scala) and [FunctionApply](https://github.com/pheymann/typedapi/blob/master/server/src/main/scala/typedapi/server/FunctionApply.scala) only provide instances up to arity 10.

If you need more you just have to provide an instance of these type-classes and you are good to go.

**Note**: I will consider adding a codegen macro in the future to generate `ApiCompilerOps` and `FunctionApply` with arity `n`.
