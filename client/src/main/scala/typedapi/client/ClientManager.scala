package typedapi.client

/** Provides a supported client instance and some basic configuration. */
final case class ClientManager[C](client: C, host: String, port: Int) {

  val base = s"$host:$port"
}
