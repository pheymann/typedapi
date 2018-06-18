package typedapi

import typedapi.shared._
import shapeless._
import shapeless.ops.hlist.Tupler
import shapeless.ops.function.FnFromProduct

package object client extends TypeLevelFoldLeftLowPrio 
                      with TypeLevelFoldLeftListLowPrio 
                      with ApiTransformer {

  def derive[H <: HList, El <: HList, KIn <: HList, VIn <: HList, M <: MethodCall, Out, D <: HList](apiList: ApiTypeCarrier[H])
                                                                                                   (implicit folder: Lazy[TypeLevelFoldLeft.Aux[H, Unit, (El, KIn, VIn, M, Out)]],
                                                                                                             builder: RequestDataBuilder.Aux[El, KIn, VIn, M, Out, D],
                                                                                                             inToFn: FnFromProduct[VIn => ExecutableDerivation[El, KIn, VIn, M, Out, D]]): inToFn.Out = 
    inToFn.apply(input => new ExecutableDerivation[El, KIn, VIn, M, Out, D](builder, input))

  def deriveAll[H <: HList, In <: HList, Fold <: HList, B <: HList, Ex <: HList](apiLists: CompositionCons[H])
                                                                                (implicit folders: TypeLevelFoldLeftList.Aux[H, Fold],
                                                                                          builderList: RequestDataBuilderList.Aux[Fold, B],
                                                                                          executables: ExecutablesFromHList.Aux[B, Ex],
                                                                                          tupler: Tupler[Ex]): tupler.Out =
    executables(builderList.builders).tupled
}
