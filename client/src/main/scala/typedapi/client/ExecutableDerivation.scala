package typedapi.client

import typedapi.shared.{MethodType, MediaType}
import shapeless._
import shapeless.labelled.FieldType

import scala.language.higherKinds

/** Helper class to match the [[RequestDataBuilder]] with an [[ApiRequest]] instance. */
final class ExecutableDerivation[El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, MT <: MediaType, O, D <: HList]
  (builder: RequestDataBuilder.Aux[El, KIn, VIn, M, FieldType[MT, O], D], input: VIn) {

  final class Derivation[F[_]] {

    def apply[C](cm: ClientManager[C])(implicit req: ApiRequest[M, D, C, F, O]): F[O] = {
      val data = builder(input, List.newBuilder, Map.empty, Map.empty)

      req(data, cm)
    }
  }

  def run[F[_]]: Derivation[F] = new Derivation[F]
}
