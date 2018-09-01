package typedapi

import typedapi.shared._
import shapeless._
import shapeless.labelled.FieldType
import shapeless.ops.hlist.{Tupler, Reverse}
import shapeless.ops.function.FnFromProduct

package object client extends TypeLevelFoldLeftLowPrio 
                      with TypeLevelFoldLeftListLowPrio
                      with WitnessToStringLowPrio {

  def deriveUriString(cm: ClientManager[_], uri: List[String]): String = cm.base + "/" + uri.mkString("/")

  def derive[H <: HList, FH <: HList, El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, MT <: MediaType, Out, D <: HList]
    (apiList: ApiTypeCarrier[H])
    (implicit filter: Lazy[TplFilter.Aux[AFilterServerElements.type, H, FH]],
              folder: Lazy[TplLeftFolder.Aux[ApiTransformer.type, FH, Unit, (El, KIn, VIn, M, FieldType[MT, Out])]],
              builder: RequestDataBuilder.Aux[El, KIn, VIn, M, FieldType[MT, Out], D],
              inToFn: FnFromProduct[VIn => ExecutableDerivation[El, KIn, VIn, M, MT, Out, D]]): inToFn.Out =
    inToFn.apply(input => new ExecutableDerivation[El, KIn, VIn, M, MT, Out, D](builder, input))

  def deriveAll[H <: HList, FH <: HList, Fold <: HList, RFold <: HList, B <: HList, Ex <: HList]
    (apiLists: CompositionCons[H])
    (implicit filter: FilterServerElementsList.Aux[H, FH],
              folders: Lazy[TplLeftFolder.Aux[ApiTransformer.type, FH, HNil, Fold]],
              reverse: Reverse.Aux[Fold, RFold],
              builderList: RequestDataBuilderList.Aux[RFold, B],
              executables: ExecutablesFromHList.Aux[B, Ex],
              tupler: Tupler[Ex]): tupler.Out =
    executables(builderList.builders).tupled
}
