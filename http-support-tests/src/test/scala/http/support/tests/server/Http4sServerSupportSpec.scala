package http.support.tests.server

import http.support.tests.{UserCoding, Api}
import typedapi.server._
import typedapi.server.http4s._
import cats.effect.IO
import org.http4s.server.blaze.BlazeBuilder

final class Http4sServerSupportSpec extends ServerSupportSpec[IO] {

  import UserCoding._

  val endpoints = deriveAll[IO](Api).from(
    path, 
    segment, 
    query, 
    header, 
    fixed, 
    input, 
    clientHdr,
    coll,
    matching, 
    send, 
    get, 
    put, 
    putB, 
    post, 
    postB, 
    delete, 
    code200, 
    code400, 
    code500
  )
  val sm        = ServerManager(BlazeBuilder[IO], "localhost", 9000)
  val server    = mount(sm, endpoints).unsafeRunSync()

  "http4s implements TypedApi's server interface" >> {
    tests(9000)

    step {
      server.shutdown.unsafeRunSync()
    }
  }

}
