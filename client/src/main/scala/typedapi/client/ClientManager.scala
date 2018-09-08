package typedapi.client

/** Provides a supported client instance and some basic configuration. */
final case class ClientManager[C](client: C, host: String, portO: Option[Int]) {

  val base = portO match {
    case Some(p) => s"$host:$p"
    case None    => host
  }
}

object ClientManager {

  def apply[C](client: C, host: String, port: Int): ClientManager[C] = ClientManager(client, host, Some(port))
  def apply[C](client: C, host: String): ClientManager[C] = ClientManager(client, host, None)
}
