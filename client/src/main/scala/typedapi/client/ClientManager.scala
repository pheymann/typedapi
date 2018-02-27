package typedapi.client

trait ClientManager[C] {

  def client: C
  def host: String
  def port: Int

  def base = s"$host:$port"
}

object ClientManager {

  def apply[C](_client: C, _host: String, _port: Int) = new ClientManager[C] {
    val client = _client
    val host   = _host
    val port   = _port
  }
}
