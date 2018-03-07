package typedapi

import typedapi.shared._
import shapeless._

package object server extends typedapi.shared.ops.ApiListOps with TypeLevelFoldLeftLowPrio with ApiTransformer with EndpointFunctionLowPrio {

  def transform[H <: HList, Out](apiList: FinalCons[H])
                                (implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil), Out]): TypeLevelFoldLeft.Aux[H, (HNil, HNil), Out] = 
    folder

  def link[H <: HList, El <: HList, In <: HList, Out, Fun](transformed: TypeLevelFoldLeft.Aux[H, (HNil, HNil), (El, In, Out)])
                                                          (implicit endpoint: EndpointFunction.Aux[In, Out, Fun]): EndpointDefinition[El, In, Out, Fun] = 
    new EndpointDefinition[El, In, Out, Fun](endpoint)
}
