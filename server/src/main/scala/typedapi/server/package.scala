package typedapi

import typedapi.shared._
import shapeless._

package object server extends EndpointFunctionLowPrio {

  def compile[H <: HList, El <: HList, In <: HList, Out, Fun](transformed: TypeLevelFoldLeft.Aux[H, (HNil, HNil), (El, In, Out)])
                                                             (implicit endpoint: EndpointFunction.Aux[In, Out, Fun]): EndpointDefinition[In, Out, Fun] = 
    new EndpointDefinition(endpoint)
}
