package typedapi.server

import shapeless._
import shapeless.ops.hlist.Prepend

import scala.language.higherKinds
import scala.annotation.implicitNotFound

@implicitNotFound("Cannot find EndpointExecutor. Do you miss some implicit values e.g. encoder/decoder?\n" + 
                  "  elements: ${El}\n  inputs:   ${In}")
sealed trait EndpointExecutor[El <: HList, In <: HList, ROut, CIn <: HList, F[_], FOut] {

  type R
  type Out

  def extract(eReq: EndpointRequest, endpoint: Endpoint[El, In, ROut, CIn, F, FOut]): Either[ExtractionError, ROut] = 
    endpoint.extractor(eReq, Set.empty, HNil)

  def apply(req: R, eReq: EndpointRequest, endpoint: Endpoint[El, In, ROut, CIn, F, FOut]): Either[ExtractionError, Out]
}

object EndpointExecutor {

  type Aux[R0, El <: HList, In <: HList, ROut, CIn <: HList, F[_], FOut, Out0] = EndpointExecutor[El, In, ROut, CIn, F, FOut] {
    type R = R0
    type Out = Out0
  }
}

trait NoReqBodyExecutor[El <: HList, In <: HList, CIn <: HList, F[_], FOut] extends EndpointExecutor[El, In, CIn, CIn, F, FOut] {

  protected def execute(input: CIn, endpoint: Endpoint[El, In, CIn, CIn, F, FOut]): F[FOut] = 
    endpoint.apply(input)
}

trait ReqBodyExecutor[El <: HList, In <: HList, Bd, ROut <: HList, POut <: HList, CIn <: HList, F[_], FOut] extends EndpointExecutor[El, In, (BodyType[Bd], ROut), CIn, F, FOut] {

  implicit def prepend: Prepend.Aux[ROut, Bd :: HNil, POut]
  implicit def eqProof: POut =:= CIn

  protected def execute(input: ROut, body: Bd, endpoint: Endpoint[El, In, (BodyType[Bd], ROut), CIn, F, FOut]): F[FOut] = 
    endpoint.apply(input :+ body)
}
