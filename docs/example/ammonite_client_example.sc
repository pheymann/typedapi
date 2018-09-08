import $ivy.`com.github.pheymann::typedapi-ammonite-client:0.2.0-M1`

import typedapi._
import client._
import amm._

val cm = clientManager("http://localhost", 9000)

final case class User(name: String, age: Int)

val Api = api(Get[Json, User], Root, headers = Headers.serverSend("Access-Control-Allow-Origin", "*"))

val get = derive(Api)

val response = get().run[Id].raw(cm)
