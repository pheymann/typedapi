package typedapi.server

import cats.Monad
import cats.effect.IO
import org.http4s._
import org.http4s.dsl._
import shapeless._
import shapeless.ops.hlist.Prepend

import scala.language.higherKinds

package object http4s {

  implicit def noReqBodyExecutor[El <: HList, In <: HList, CIn <: HList, F[_]: Monad, FOut](implicit encoder: EntityEncoder[F, FOut]) = new NoReqBodyExecutor[El, In, CIn, F, FOut] {
    type R   = Request[F]
    type Out = F[Response[F]]

    def apply(req: R, eReq: EndpointRequest, endpoint: Endpoint[El, In, CIn, CIn, F, FOut]): Option[Out] = {
      extract(eReq, endpoint).map { extracted =>
        Monad[F].flatMap(execute(extracted, endpoint)) { response =>
          val resp: F[Response[F]] = Ok.apply(response)(Monad[F], encoder)

          resp
        }
      }
    }
  }

  implicit def withReqBodyExecutor[El <: HList, In <: HList, Bd, ROut <: HList, POut <: HList, CIn <: HList, FOut]
    (implicit encoder: EntityEncoder[IO, FOut], 
              decoder: EntityDecoder[IO, Bd],
              _prepend: Prepend.Aux[ROut, Bd :: HNil, POut], 
              _eqProof: POut =:= CIn) = new ReqBodyExecutor[El, In, Bd, ROut, POut, CIn, IO, FOut] {
    type R   = Request[IO]
    type Out = IO[Response[IO]]

    implicit val prepend = _prepend
    implicit val eqProof = _eqProof

    def apply(req: R, eReq: EndpointRequest, endpoint: Endpoint[El, In, (BodyType[Bd], ROut), CIn, IO, FOut]): Option[Out] = {
      extract(eReq, endpoint).map { case (_, extracted) =>
        for {
          body     <- req.as[Bd]
          response <- execute(extracted, body, endpoint)

          r <- Ok(response)
        } yield r
      }
    }
  }

//  implicit val mountEndpoints = new MountEndpoints[BlazeBuilder[IO], Request[IO], IO[Response[IO]]] {

//  }
}
