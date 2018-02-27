package typedapi.client

import scala.language.higherKinds

package object test {

  type TestClientM = ClientManager[Unit]

  val clientManager: TestClientM = ClientManager((), "", 0)

  def testGet[F[_], A](f: ReqInput => F[A]) = new GetRequest[Unit, F, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[A] = f(ReqInput("GET", uri, queries, headers))
  }

  def testPut[F[_], A](f: ReqInput => F[A]) = new PutRequest[Unit, F, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[A] = f(ReqInput("PUT", uri, queries, headers))
  }

  def testPutWithBody[F[_], Bd, A](f: ReqInputWithBody[Bd] => F[A]) = new PutWithBodyRequest[Unit, F, Bd, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: TestClientM): F[A] = f(ReqInputWithBody("PUT", uri, queries, headers, body))
  }

  def testPost[F[_], A](f: ReqInput => F[A]) = new PostRequest[Unit, F, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[A] = f(ReqInput("POST", uri, queries, headers))
  }

  def testPostWithBody[F[_], Bd, A](f: ReqInputWithBody[Bd] => F[A]) = new PostWithBodyRequest[Unit, F, Bd, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: TestClientM): F[A] = f(ReqInputWithBody("POST", uri, queries, headers, body))
  }

  def testDelete[F[_], A](f: ReqInput => F[A]) = new DeleteRequest[Unit, F, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[A] = f(ReqInput("DELETE", uri, queries, headers))
  }
}
