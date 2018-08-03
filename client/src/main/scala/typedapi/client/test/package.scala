package typedapi.client

import scala.language.higherKinds

package object test {

  type TestClientM = ClientManager[Unit]

  val clientManager: TestClientM = ClientManager((), "", 0)

  def testGet[F[_], A](f: ReqInput => F[A])(pure: ReqInput => F[ReqInput]) = new GetRequest[Unit, F, A] {
    type Resp = ReqInput

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[Resp] = pure(ReqInput("GET", uri, queries, headers))
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[A] = f(ReqInput("GET", uri, queries, headers))
  }

  def testPut[F[_], A](f: ReqInput => F[A])(pure: ReqInput => F[ReqInput]) = new PutRequest[Unit, F, A] {
    type Resp = ReqInput

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[Resp] = pure(ReqInput("PUT", uri, queries, headers))
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[A] = f(ReqInput("PUT", uri, queries, headers))
  }

  def testPutWithBody[F[_], Bd, A](f: ReqInputWithBody[Bd] => F[A])(pure: ReqInputWithBody[Bd] => F[ReqInputWithBody[Bd]]) = new PutWithBodyRequest[Unit, F, Bd, A] {
    type Resp = ReqInputWithBody[Bd]

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: TestClientM): F[Resp] = pure(ReqInputWithBody("PUT", uri, queries, headers, body))
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: TestClientM): F[A] = f(ReqInputWithBody("PUT", uri, queries, headers, body))
  }

  def testPost[F[_], A](f: ReqInput => F[A])(pure: ReqInput => F[ReqInput]) = new PostRequest[Unit, F, A] {
    type Resp = ReqInput

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[Resp] = pure(ReqInput("POST", uri, queries, headers))
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[A] = f(ReqInput("POST", uri, queries, headers))
  }

  def testPostWithBody[F[_], Bd, A](f: ReqInputWithBody[Bd] => F[A])(pure: ReqInputWithBody[Bd] => F[ReqInputWithBody[Bd]]) = new PostWithBodyRequest[Unit, F, Bd, A] {
    type Resp = ReqInputWithBody[Bd]

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: TestClientM): F[Resp] = pure(ReqInputWithBody("POST", uri, queries, headers, body))
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: TestClientM): F[A] = f(ReqInputWithBody("POST", uri, queries, headers, body))
  }

  def testDelete[F[_], A](f: ReqInput => F[A])(pure: ReqInput => F[ReqInput]) = new DeleteRequest[Unit, F, A] {
    type Resp = ReqInput

    def raw(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[Resp] = pure(ReqInput("DELETE", uri, queries, headers))
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[A] = f(ReqInput("DELETE", uri, queries, headers))
  }
}
