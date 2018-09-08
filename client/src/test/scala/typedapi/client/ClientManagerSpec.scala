package typedapi.client

import org.specs2.mutable.Specification

final class ClientManagerSpec extends Specification {

  "optional port definition" >> {
    ClientManager((), "my-host", 80).base === "my-host:80"
    ClientManager((), "my-host").base === "my-host"
  }
}
