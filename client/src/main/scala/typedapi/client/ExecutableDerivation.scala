package typedapi.client

import shapeless._

import scala.language.higherKinds

final class ExecutableDerivation[El <: HList, KIn <: HList, VIn <: HList, O, D <: HList](builder: RequestDataBuilder.Aux[El, KIn, VIn, O, D], input: VIn) {

  final class Derivation[F[_]] {

    def apply[C](cm: ClientManager[C])(implicit req: ApiRequest[D, C, F, O]): F[O] = {
      val data = builder(input, List.newBuilder, Map.empty, Map.empty)

      req(data, cm)
    }
  }

  def run[F[_]]: Derivation[F] = new Derivation[F]
}
