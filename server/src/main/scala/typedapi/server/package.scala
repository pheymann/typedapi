package typedapi

import typedapi.shared._
import shapeless._

package object server extends TypeLevelFoldLeftLowPrio with ApiTransformer with typedapi.shared.ops.ApiListOps with EndpointFunctionLowPrio {

  def link[H <: HList, El <: HList, In <: HList, Out, Fun](transformed: TypeLevelFoldLeft.Aux[H, (HNil, HNil), (El, In, Out)])
                                                          (implicit endpoint: EndpointFunction.Aux[In, Out, Fun]): EndpointDefinition[El, In, Out, Fun] = 
    new EndpointDefinition[El, In, Out, Fun](endpoint)
}
