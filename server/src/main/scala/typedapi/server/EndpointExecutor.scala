package typedapi.server

import shapeless._
import shapeless.ops.hlist.Reverse

sealed trait EndpointExecutor[R, El <: HList, In <: HList, ROut, CIn <: HList, Fun, FOut] {

  type Out

  def extract(eReq: EndpointRequest, endpoint: Endpoint[El, In, ROut, CIn, FOut, Fun]): Option[ROut] = 
    endpoint.extractor(eReq, Set.empty, HNil)

  def apply(req: R, eReq: EndpointRequest, endpoint: Endpoint[El, In, ROut, CIn, FOut, Fun]): Option[Out]
}

object EndpointExecutor {

  type Aux[R, El <: HList, In <: HList, ROut, CIn <: HList, Fun, FOut, Out0] = EndpointExecutor[R, El, In, ROut, CIn, Fun, FOut] {
    type Out = Out0
  }
}

trait NoReqBodyExecutor[R, El <: HList, In <: HList, ROut <: HList, CIn <: HList, Fun, FOut] extends EndpointExecutor[R, El, In, ROut, CIn, Fun, FOut] {

  implicit def rev: Reverse.Aux[ROut, CIn]

  protected def execute(input: ROut, endpoint: Endpoint[El, In, ROut, CIn, FOut, Fun]): FOut = 
    endpoint.apply(input.reverse)
}

trait ReqBodyExecutor[R, El <: HList, In <: HList, Bd , ROut <: HList, CIn <: HList, Fun, FOut] extends EndpointExecutor[R, El, In, (BodyType[Bd], ROut), CIn, Fun, FOut] {

  implicit def rev: Reverse.Aux[Bd :: ROut, CIn]

  protected def execute(input: ROut, body: Bd, endpoint: Endpoint[El, In, ROut, CIn, FOut, Fun]): FOut = 
    endpoint.apply((body :: input).reverse)
}
