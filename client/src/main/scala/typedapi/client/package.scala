package typedapi

import typedapi.shared._
import shapeless._
import shapeless.ops.hlist.Tupler
import shapeless.ops.function.FnFromProduct

package object client extends TypeLevelFoldLeftLowPrio 
                      with TypeLevelFoldLeftListLowPrio 
                      with ApiTransformer 
                      with FoldResultEvidenceLowPrio {

  type Transformed[El <: HList, KIn <: HList, VIn <: HList, Out, D <: HList] = (El, KIn, VIn, Out)

  def derive[H <: HList, Fold, El <: HList, KIn <: HList, VIn <: HList, Out, D <: HList](apiList: ApiTypeCarrier[H])
                                                                                        (implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil, HNil), Fold],
                                                                                                  ev: FoldResultEvidence.Aux[Fold, El, KIn, VIn, Out],
                                                                                                  builder: RequestDataBuilder.Aux[El, KIn, VIn, Out, D],
                                                                                                  inToFn: FnFromProduct[VIn => ExecutableDerivation[El, KIn, VIn, Out, D]]): inToFn.Out = 
    inToFn.apply(input => new ExecutableDerivation[El, KIn, VIn, Out, D](builder, input))

  def deriveAll[H <: HList, In <: HList, Fold <: HList, B <: HList, Ex <: HList](apiLists: CompositionCons[H])
                                                                                (implicit folders: TypeLevelFoldLeftList.Aux[H, Fold],
                                                                                          builderList: RequestDataBuilderList.Aux[Fold, B],
                                                                                          executables: ExecutablesFromHList.Aux[B, Ex],
                                                                                          tupler: Tupler[Ex]): tupler.Out =
    executables(builderList.builders).tupled
}
