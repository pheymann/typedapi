package typedapi

import typedapi.shared._
import shapeless._
import shapeless.ops.function.FnFromProduct

package object client extends HListToCompositionLowPrio 
                      with TypeLevelFoldLeftLowPrio 
                      with TypeLevelFoldLeftListLowPrio 
                      with ApiTransformer 
                      with FoldResultEvidenceLowPrio
                      with ApiCompilerMediumPrio 
                      with ApiCompilerListLowPrio {

  type Transformed[El <: HList, KIn <: HList, VIn <: HList, Out, D <: HList] = (El, KIn, VIn, Out)

  def derive[H <: HList, Fold, El <: HList, KIn <: HList, VIn <: HList, Out, D <: HList](apiList: ApiTypeCarrier[H])
                                                                                        (implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil, HNil), Fold],
                                                                                                  ev: FoldResultEvidence.Aux[Fold, El, KIn, VIn, Out],
                                                                                                  compiler: ApiCompiler.Aux[El, KIn, VIn, Out, D],
                                                                                                  inToFn: FnFromProduct[VIn => ExecutableDerivation[El, KIn, VIn, Out, D]]): inToFn.Out = 
    inToFn.apply(input => new ExecutableDerivation[El, KIn, VIn, Out, D](compiler, input))

  def deriveAll[H <: HList, In <: HList, Fold <: HList, HL <: HList, Out <: HList](apiLists: CompositionCons[H])
                                                                                  (implicit folders: TypeLevelFoldLeftList.Aux[H, Fold],
                                                                                            compilers: ApiCompilerList.Aux[Fold, HL], 
                                                                                            composition: HListToComposition[HL]): composition.Out =
    composition(compilers.compilers)
}
