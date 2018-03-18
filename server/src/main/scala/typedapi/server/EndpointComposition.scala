package typedapi.server

import shapeless._

import scala.language.higherKinds

final case class EndpointComposition[End <: HList](endpoints: End) {

  def :|:[El <: HList, In <: HList, ROut, CIn <: HList, F[_], Out](endpoint: Endpoint[El, In, ROut, CIn, F, Out])
      : EndpointComposition[Endpoint[El, In, ROut, CIn, F, Out] :: End] =
    EndpointComposition(endpoint :: endpoints)
}

object =: {

  def :|:[El <: HList, In <: HList, ROut, CIn <: HList, F[_], Out](endpoint: Endpoint[El, In, ROut, CIn, F, Out])
      : EndpointComposition[Endpoint[El, In, ROut, CIn, F, Out] :: HNil] =
    EndpointComposition(endpoint :: HNil)
}
