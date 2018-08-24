package http.support.tests.client

import http.support.tests.{UserCoding, User, Api}
import typedapi.client._
import typedapi.client.scalajhttp._
import scalaj.http.Http
import io.circe.parser._
import io.circe.syntax._
import org.specs2.mutable.Specification

final class ScalajHttpClientSupportSpec extends Specification {

  import UserCoding._

  sequential

  case class DecodeException(msg: String) extends Exception

  implicit val decoder = typedapi.util.Decoder[Id, User](json => decode[User](json).fold(
    error => Left(DecodeException(error.toString())),
    user  => Right(user)
  ))
  implicit val encoder = typedapi.util.Encoder[Id, User](user => user.asJson.noSpaces)

  val cm = ClientManager(Http, "http://localhost", 9001)

  val server = TestServer.start()

  "http4s client support" >> {
    val (p, s, q, header, fixed, clInH, clFixH, serMatchH, serSendH, m0, m1, m2, m3, m4, m5, code200, code400, code500) = deriveAll(Api)

    "paths and segments" >> {
      p().run[Blocking](cm) === Right(User("foo", 27))
      s("jim").run[Blocking](cm) === Right(User("jim", 27))
    }
    
    "queries" >> {
      q(42).run[Blocking](cm) === Right(User("foo", 42))
    }
    
    "headers" >> {
      header(42).run[Blocking](cm) === Right(User("foo", 42))
      fixed().run[Blocking](cm) === Right(User("joe", 27))
      clInH("jim").run[Blocking](cm) === Right(User("jim", 27))
      clFixH().run[Blocking](cm) === Right(User("joe", 27))
      serMatchH().run[Blocking](cm) === Right(User("joe", 27))
      serSendH().run[Blocking](cm) === Right(User("joe", 27))
    }

    "methods" >> {
      m0().run[Blocking](cm) === Right(User("foo", 27))
      m1().run[Blocking](cm) === Right(User("foo", 27))
      m2(User("jim", 42)).run[Blocking](cm) === Right(User("jim", 42))
      m3().run[Blocking](cm) === Right(User("foo", 27))
      m4(User("jim", 42)).run[Blocking](cm) === Right(User("jim", 42))
      m5(List("because")).run[Blocking](cm) === Right(User("foo", 27))
    }

    "raw" >> {
      m0().run[Id].raw(cm).body === """{"name":"foo","age":27}"""
    }

    step {
      server.shutdown.unsafeRunSync()
    }
  }
}
