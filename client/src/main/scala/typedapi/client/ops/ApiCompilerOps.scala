package typedapi.client.ops

import typedapi.client.{ClientManager, TypedApi, ApiCompiler}

import shapeless._
import shapeless.labelled.{FieldType, field}

abstract class BaseApiOps[El <: HList, In <: HList, O, D <: HList](compiler: ApiCompiler.Aux[El, In, O, D]) {

  def apply[I, C](input: I)(implicit cm: ClientManager[C]) = TypedApi.execute(compiler, input, cm)
}

sealed trait ApiCompilerOpsLowPrio {

  implicit class ApiOps1[El <: HList, O, D <: HList,  K0, V0](compiler: ApiCompiler.Aux[El, FieldType[K0, V0] :: HNil, O, D]) extends BaseApiOps(compiler) {

    def apply[C](in0: V0)(implicit cm: ClientManager[C]) = TypedApi.execute(compiler, field[K0](in0) :: HNil, cm)
  }

  type In2[K0, V0, K1, V1] = FieldType[K0, V0] :: FieldType[K1, V1] :: HNil

  implicit class ApiOps2[El <: HList, O, D <: HList,  K0, V0, K1, V1](compiler: ApiCompiler.Aux[El, In2[K0, V0, K1, V1], O, D]) extends BaseApiOps(compiler) {

    def apply[C](in0: V0, in1: V1)(implicit cm: ClientManager[C]) = TypedApi.execute(compiler, field[K0](in0) :: field[K1](in1) :: HNil, cm)
  }

  type In3[K0, V0, K1, V1, K2, V2] = FieldType[K0, V0] :: FieldType[K1, V1] :: FieldType[K2, V2] :: HNil

  implicit class ApiOps3[El <: HList, O, D <: HList,  K0, V0, K1, V1, K2, V2](compiler: ApiCompiler.Aux[El, In3[K0, V0, K1, V1, K2, V2], O, D]) extends BaseApiOps(compiler) {

    def apply[C](in0: V0, in1: V1, in2: V2)(implicit cm: ClientManager[C]) = TypedApi.execute(
      compiler, 
      field[K0](in0) :: field[K1](in1) :: field[K2](in2) :: HNil, 
      cm
    )
  }

  type In4[K0, V0, K1, V1, K2, V2, K3, V3] = FieldType[K0, V0] :: FieldType[K1, V1] :: FieldType[K2, V2] :: FieldType[K3, V3] :: HNil

  implicit class ApiOps4[El <: HList, O, D <: HList,  K0, V0, K1, V1, K2, V2, K3, V3](compiler: ApiCompiler.Aux[El, In4[K0, V0, K1, V1, K2, V2, K3, V3], O, D]) extends BaseApiOps(compiler) {

    def apply[C](in0: V0, in1: V1, in2: V2, in3: V3)(implicit cm: ClientManager[C]) = TypedApi.execute(
      compiler, 
      field[K0](in0) :: field[K1](in1) :: field[K2](in2) :: field[K3](in3) :: HNil, 
      cm
    )
  }

  type In5[K0, V0, K1, V1, K2, V2, K3, V3, K4, V4] = FieldType[K0, V0] :: FieldType[K1, V1] :: FieldType[K2, V2] :: FieldType[K3, V3] :: FieldType[K4, V4] :: HNil

  implicit class ApiOps5[El <: HList, O, D <: HList,  K0, V0, K1, V1, K2, V2, K3, V3, K4, V4](compiler: ApiCompiler.Aux[El, In5[K0, V0, K1, V1, K2, V2, K3, V3, K4, V4], O, D]) extends BaseApiOps(compiler) {

    def apply[C](in0: V0, in1: V1, in2: V2, in3: V3, in4: V4)(implicit cm: ClientManager[C]) = TypedApi.execute(
      compiler, 
      field[K0](in0) :: field[K1](in1) :: field[K2](in2) :: field[K3](in3) :: field[K4](in4) :: HNil, 
      cm
    )
  }

  type In6[K0, V0, K1, V1, K2, V2, K3, V3, K4, V4, K5, V5] = FieldType[K0, V0] :: FieldType[K1, V1] :: FieldType[K2, V2] :: FieldType[K3, V3] :: FieldType[K4, V4] :: FieldType[K5, V5] :: HNil

  implicit class ApiOps6[El <: HList, O, D <: HList,  K0, V0, K1, V1, K2, V2, K3, V3, K4, V4, K5, V5](compiler: ApiCompiler.Aux[El, In6[K0, V0, K1, V1, K2, V2, K3, V3, K4, V4, K5, V5], O, D]) extends BaseApiOps(compiler) {

    def apply[C](in0: V0, in1: V1, in2: V2, in3: V3, in4: V4, in5: V5)(implicit cm: ClientManager[C]) = TypedApi.execute(
      compiler, 
      field[K0](in0) :: field[K1](in1) :: field[K2](in2) :: field[K3](in3) :: field[K4](in4) :: field[K5](in5) :: HNil, 
      cm
    )
  }

  type In7[K0, V0, K1, V1, K2, V2, K3, V3, K4, V4, K5, V5, K6, V6] = FieldType[K0, V0] :: FieldType[K1, V1] :: FieldType[K2, V2] :: FieldType[K3, V3] :: FieldType[K4, V4] :: FieldType[K5, V5] :: FieldType[K6, V6] :: HNil

  implicit class ApiOps7[El <: HList, O, D <: HList,  K0, V0, K1, V1, K2, V2, K3, V3, K4, V4, K5, V5, K6, V6](compiler: ApiCompiler.Aux[El, In7[K0, V0, K1, V1, K2, V2, K3, V3, K4, V4, K5, V5, K6, V6], O, D]) extends BaseApiOps(compiler) {

    def apply[C](in0: V0, in1: V1, in2: V2, in3: V3, in4: V4, in5: V5, in6: V6)(implicit cm: ClientManager[C]) = TypedApi.execute(
      compiler, 
      field[K0](in0) :: field[K1](in1) :: field[K2](in2) :: field[K3](in3) :: field[K4](in4) :: field[K5](in5) :: field[K6](in6) :: HNil, 
      cm
    )
  }

  type In8[K0, V0, K1, V1, K2, V2, K3, V3, K4, V4, K5, V5, K6, V6, K7, V7] = FieldType[K0, V0] :: FieldType[K1, V1] :: FieldType[K2, V2] :: FieldType[K3, V3] :: FieldType[K4, V4] :: FieldType[K5, V5] :: FieldType[K6, V6] :: FieldType[K7, V7] :: HNil

  implicit class ApiOps8[El <: HList, O, D <: HList,  K0, V0, K1, V1, K2, V2, K3, V3, K4, V4, K5, V5, K6, V6, K7, V7](compiler: ApiCompiler.Aux[El, In8[K0, V0, K1, V1, K2, V2, K3, V3, K4, V4, K5, V5, K6, V6, K7, V7], O, D]) extends BaseApiOps(compiler) {

    def apply[C](in0: V0, in1: V1, in2: V2, in3: V3, in4: V4, in5: V5, in6: V6, in7: V7)(implicit cm: ClientManager[C]) = TypedApi.execute(
      compiler, 
      field[K0](in0) :: field[K1](in1) :: field[K2](in2) :: field[K3](in3) :: field[K4](in4) :: field[K5](in5) :: field[K6](in6) :: field[K7](in7) :: HNil, 
      cm
    )
  }

  implicit class ApiOpsN[El <: HList, O, D <: HList,  In <: HList](compiler: ApiCompiler.Aux[El, In, O, D]) extends BaseApiOps(compiler)
}


trait ApiCompilerOps extends ApiCompilerOpsLowPrio {

  implicit class ApiOps0[El <: HList, O, D <: HList](compiler: ApiCompiler.Aux[El, HNil, O, D]) {

    def apply[C]()(implicit cm: ClientManager[C]) = new TypedApi.EmptyExecutionHelper[El, O, D, C](compiler, cm)
  }
}
