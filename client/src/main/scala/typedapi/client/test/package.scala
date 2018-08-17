package typedapi.client

import scala.language.higherKinds

package object test {

  type TestClientM = ClientManager[Unit]

  val clientManager: TestClientM = ClientManager((), "", 0)

  def testRawGet[F[_]](pure: ReqInput => F[ReqInput]) = new RawGetRequest[Unit, F] {
    type Resp = ReqInput

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[Resp] = 
      pure(ReqInput("GET", uri, queries, headers))
  }
  def testGet[F[_], A](f: ReqInput => F[A]) = new GetRequest[Unit, F, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[A] = 
      f(ReqInput("GET", uri, queries, headers))
  }

  def testRawPut[F[_]](pure: ReqInput => F[ReqInput]) = new RawPutRequest[Unit, F] {
    type Resp = ReqInput

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[Resp] = 
      pure(ReqInput("PUT", uri, queries, headers))
  }
  def testPut[F[_], A](f: ReqInput => F[A]) = new PutRequest[Unit, F, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[A] = 
      f(ReqInput("PUT", uri, queries, headers))
  }

  def testRawPutWithBody[F[_], Bd](pure: ReqInputWithBody[Bd] => F[ReqInputWithBody[Bd]]) = 
    new RawPutWithBodyRequest[Unit, F, Bd] {
      type Resp = ReqInputWithBody[Bd]

      def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: TestClientM): F[Resp] = 
        pure(ReqInputWithBody("PUT", uri, queries, headers, body))
    }
  def testPutWithBody[F[_], Bd, A](f: ReqInputWithBody[Bd] => F[A]) = 
    new PutWithBodyRequest[Unit, F, Bd, A] {
      def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: TestClientM): F[A] = 
        f(ReqInputWithBody("PUT", uri, queries, headers, body))
    }

  def testRawPost[F[_]](pure: ReqInput => F[ReqInput]) = new RawPostRequest[Unit, F] {
    type Resp = ReqInput

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[Resp] = 
      pure(ReqInput("POST", uri, queries, headers))
  }
  def testPost[F[_], A](f: ReqInput => F[A]) = new PostRequest[Unit, F, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[A] = 
      f(ReqInput("POST", uri, queries, headers))
  }

  def testRawPostWithBody[F[_], Bd](pure: ReqInputWithBody[Bd] => F[ReqInputWithBody[Bd]]) = 
    new RawPostWithBodyRequest[Unit, F, Bd] {
    type Resp = ReqInputWithBody[Bd]

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: TestClientM): F[Resp] = 
      pure(ReqInputWithBody("POST", uri, queries, headers, body))
  }
  def testPostWithBody[F[_], Bd, A](f: ReqInputWithBody[Bd] => F[A]) = 
    new PostWithBodyRequest[Unit, F, Bd, A] {
      def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], body: Bd, cm: TestClientM): F[A] = 
        f(ReqInputWithBody("POST", uri, queries, headers, body))
    }

  def testRawDelete[F[_]](pure: ReqInput => F[ReqInput]) = new RawDeleteRequest[Unit, F] {
    type Resp = ReqInput

    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[Resp] = 
      pure(ReqInput("DELETE", uri, queries, headers))
  }
  def testDelete[F[_], A](f: ReqInput => F[A]) = new DeleteRequest[Unit, F, A] {
    def apply(uri: List[String], queries: Map[String, List[String]], headers: Map[String, String], cm: TestClientM): F[A] = 
      f(ReqInput("DELETE", uri, queries, headers))
  }
}
