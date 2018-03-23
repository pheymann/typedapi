package typedapi.client

final case class ClientManager[C](client: C, host: String, port: Int) {

  val base = s"$host:$port"
}
