package typedapi.client

import shapeless._

import scala.language.higherKinds

object TypedApi {

  final class ExecutionHelper[El <: HList, In <: HList, D <: HList, I, C](val compiler: ApiCompiler.Aux[El, In, D], input: I, cm: ClientManager[C]) {

    def run[F[_]](implicit gen: LabelledGeneric.Aux[I, In], req: ApiRequest[D, C, F]): F[req.Out] = {
      val data = compiler(gen.to(input), List.newBuilder, Map.empty, Map.empty)

      req(data, cm)
    }
  }

  final class RawExecutionHelper[El <: HList, In <: HList, D <: HList, C](val compiler: ApiCompiler.Aux[El, In, D], input: In, cm: ClientManager[C]) {

    def run[F[_]](implicit req: ApiRequest[D, C, F]): F[req.Out] = {
      val data = compiler(input, List.newBuilder, Map.empty, Map.empty)

      req(data, cm)
    }
  }

  final class EmptyExecutionHelper[El <: HList, D <: HList, C](val compiler: ApiCompiler.Aux[El, HNil, D], cm: ClientManager[C]) {

    def run[F[_]](implicit req: ApiRequest[D, C, F]): F[req.Out] = {
      val data = compiler(HNil, List.newBuilder, Map.empty, Map.empty)

      req(data, cm)
    }
  }

  def execute[El <: HList, In <: HList, D <: HList, I, C](compiler: ApiCompiler.Aux[El, In, D], input: I, cm: ClientManager[C]) = 
    new ExecutionHelper[El, In, D, I, C](compiler, input, cm)

  def execute[El <: HList, In <: HList, D <: HList, C](compiler: ApiCompiler.Aux[El, In, D], input: In, cm: ClientManager[C]) = 
    new RawExecutionHelper[El, In, D, C](compiler, input, cm)

  def execute[El <: HList, D <: HList, C](compiler: ApiCompiler.Aux[El, HNil, Data], cm: ClientManager[C]) = 
    new EmptyExecutionHelper[El, Data, C](compiler, cm)
}
