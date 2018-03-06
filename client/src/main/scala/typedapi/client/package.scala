package typedapi

import typedapi.shared._
import shapeless._

package object client extends HListToCompositionLowPrio with TypeLevelFoldLeftLowPrio with TypeLevelFoldLeftListLowPrio with ApiTransformer with ApiCompilerMediumPrio with ApiCompilerListLowPrio with ops.ApiCompilerOps with typedapi.shared.ops.ApiListOps {

  type Transformed[El <: HList, In <: HList, Out, D <: HList] = (El, In, Out)

  def compile[H <: HList, El <: HList, In <: HList, O, D <: HList](folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil), (El, In, O)])
                                                                  (implicit compiler: ApiCompiler.Aux[El, In, O, D]): ApiCompiler.Aux[El, In, O, D] = 
    compiler

  def transform[H <: HList, In <: HList, Out <: HList](apiLists: CompositionCons[H])
                                                      (implicit folders: TypeLevelFoldLeftList.Aux[H, In, Out]): TypeLevelFoldLeftList.Aux[H, In, Out] =
    folders

  def compile[H <: HList, In <: HList, Fold <: HList, HL <: HList, Out <: HList](folders: TypeLevelFoldLeftList.Aux[H, In, Fold])
                                                                                (implicit compilers: ApiCompilerList.Aux[Fold, HL], composition: HListToComposition[HL]): composition.Out =
    composition(compilers.compilers)
}
