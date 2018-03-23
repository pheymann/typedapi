package typedapi.client

import shapeless._

import scala.language.higherKinds

object TypedApi {

  final class ExecutionHelper[El <: HList, In <: HList, O, D <: HList, I, C](val compiler: ApiCompiler.Aux[El, In, O, D], input: I, cm: ClientManager[C]) {

    def run[F[_]](implicit gen: LabelledGeneric.Aux[I, In], req: ApiRequest[D, C, F, O]): F[O] = {
      val data = compiler(gen.to(input), List.newBuilder, Map.empty, Map.empty)

      req(data, cm)
    }
  }

  final class RawExecutionHelper[El <: HList, In <: HList, O, D <: HList, C](val compiler: ApiCompiler.Aux[El, In, O, D], input: In, cm: ClientManager[C]) {

    def run[F[_]](implicit req: ApiRequest[D, C, F, O]): F[O] = {
      val data = compiler(input, List.newBuilder, Map.empty, Map.empty)

      req(data, cm)
    }
  }

  final class EmptyExecutionHelper[El <: HList, O, D <: HList, C](val compiler: ApiCompiler.Aux[El, HNil, O, D], cm: ClientManager[C]) {

    def run[F[_]](implicit req: ApiRequest[D, C, F, O]): F[O] = {
      val data = compiler(HNil, List.newBuilder, Map.empty, Map.empty)

      req(data, cm)
    }
  }

  def execute[El <: HList, In <: HList, O, D <: HList, I, C](compiler: ApiCompiler.Aux[El, In, O, D], input: I, cm: ClientManager[C]) = 
    new ExecutionHelper[El, In, O, D, I, C](compiler, input, cm)

  def execute[El <: HList, In <: HList, O, D <: HList, C](compiler: ApiCompiler.Aux[El, In, O, D], input: In, cm: ClientManager[C]) = 
    new RawExecutionHelper[El, In, O, D, C](compiler, input, cm)

  def execute[El <: HList, O, D <: HList, C](compiler: ApiCompiler.Aux[El, HNil, O, Data], cm: ClientManager[C]) = 
    new EmptyExecutionHelper[El, O, Data, C](compiler, cm)
}
