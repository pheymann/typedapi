package typedapi

import shapeless._

package object client extends HListToCompositionLowPrio with TypeLevelFoldLeftLowPrio with TypeLevelFoldLeftListLowPrio with ApiTransformer with ApiCompilerMediumPrio with ApiCompilerListLowPrio with ops.ApiCompilerOps {

  def := = EmptyCons

  def Segment[A] = new SegmentHelper[A]
  def Query[A]   = new QueryHelper[A]
  def Header[A]  = new HeaderHelper[A]

  final val BodyField       = Witness('body)
  final val RawHeadersField = Witness('rawHeaders)

  type Transformed[El <: HList, In <: HList, D <: HList] = (El, In)

  def transform[H <: HList, Out](apiList: FinalCons[H])
                                (implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil), Out]): TypeLevelFoldLeft.Aux[H, (HNil, HNil), Out] = 
    folder

  def compile[H <: HList, El <: HList, In <: HList, D <: HList](folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil), (El, In)])
                                                               (implicit compiler: ApiCompiler.Aux[El, In, D]): ApiCompiler.Aux[El, In, D] = 
    compiler

  def transform[H <: HList, In <: HList, Out <: HList](apiLists: CompositionCons[H])
                                                      (implicit folders: TypeLevelFoldLeftList.Aux[H, In, Out]): TypeLevelFoldLeftList.Aux[H, In, Out] =
    folders

  def compile[H <: HList, In <: HList, Fold <: HList, HL <: HList, Out <: HList](folders: TypeLevelFoldLeftList.Aux[H, In, Fold])
                                                                                (implicit compilers: ApiCompilerList.Aux[Fold, HL], composition: HListToComposition[HL]): composition.Out =
    composition(compilers.compilers)
}
