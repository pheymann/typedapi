package typedapi.server

import shapeless.HList

final class EndpointDefinition[H <: HList, Out, Fun](fun: EndpointFunction.Aux[H, Out, Fun]) {

  def define(f: Fun): Fun = f
}
