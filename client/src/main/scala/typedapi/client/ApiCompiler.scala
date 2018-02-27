package typedapi.client

import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.collection.mutable.Builder
import scala.annotation.implicitNotFound

/** Compiles type level api description into a function returning data (uri, query, header, body) and return-type `A` which are used for a request.
  */
@implicitNotFound("Cannot find ApiExecutor instance for:\n - elements: ${El}\n - inputs: ${In}")
trait ApiCompiler[El <: HList, In <: HList] {

  type Out <: HList

  def apply(inputs: In, 
            uri: Builder[String, List[String]], 
            queries: Map[String, List[String]], 
            headers: Map[String, String]): Out
}

object ApiCompiler {

  type Aux[El <: HList, In <: HList, Out0 <: HList] = ApiCompiler[El, In] { type Out = Out0 }
}

trait ApiCompilerLowPrio {

  implicit def pathCompiler[S, T <: HList, In <: HList](implicit wit: Witness.Aux[S], compiler: ApiCompiler[T, In]) = new ApiCompiler[S :: T, In] {
    type Out = compiler.Out

    def apply(inputs: In, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      compiler(inputs, uri += wit.value.toString(), queries, headers)
    }
  }
  
  implicit def segmentInputCompiler[T <: HList, K, V, In <: HList](implicit compiler: ApiCompiler[T, In]) = new ApiCompiler[SegmentInput :: T, FieldType[K, V] :: In] {
    type Out = compiler.Out

    def apply(inputs: FieldType[K, V] :: In, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val segValue: V = inputs.head

      compiler(inputs.tail, uri += segValue.toString(), queries, headers)
    }
  }

  implicit def queryInputCompiler[T <: HList, K <: Symbol, V, In <: HList](implicit wit: Witness.Aux[K], compiler: ApiCompiler[T, In]) = new ApiCompiler[QueryInput :: T, FieldType[K, V] :: In] {
    type Out = compiler.Out

    def apply(inputs: FieldType[K, V] :: In, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val queryName     = wit.value.name
      val queryValue: V = inputs.head

      compiler(inputs.tail, uri, Map((queryName, List(queryValue.toString()))) ++ queries, headers)
    }
  }

  implicit def headersInputCompiler[T <: HList, K <: Symbol, V, In <: HList](implicit wit: Witness.Aux[K], compiler: ApiCompiler[T, In]) = new ApiCompiler[HeaderInput :: T, FieldType[K, V] :: In] {
    type Out = compiler.Out

    def apply(inputs: FieldType[K, V] :: In, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val headersName     = wit.value.name
      val headersValue: V = inputs.head

      compiler(inputs.tail, uri, queries, Map((headersName, headersValue.toString())) ++ headers)
    }
  }

  implicit def rawHeadersInputCompiler[T <: HList, In <: HList](implicit compiler: ApiCompiler[T, In]) = new ApiCompiler[RawHeadersInput :: T, FieldType[RawHeadersField.T, Map[String, String]] :: In] {
    type Out = compiler.Out

    def apply(inputs: FieldType[RawHeadersField.T, Map[String, String]] :: In, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val headerMap: Map[String, String] = inputs.head

      compiler(inputs.tail, uri, queries, headerMap ++ headers)
    }
  }

  type Data                       = List[String] :: Map[String, List[String]] :: Map[String, String] :: HNil
  type DataWithBody[Bd]           = List[String] :: Map[String, List[String]] :: Map[String, String] :: Bd :: HNil
  type RequestData[R, D <: HList] = FieldType[R, D] :: HNil

  implicit def getCompiler[A] = new ApiCompiler[GetCall[A] :: HNil, HNil] {
    type Out = RequestData[GetCall[A], Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[GetCall[A]](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }

  implicit def putCompiler[A] = new ApiCompiler[PutCall[A] :: HNil, HNil] {
    type Out = RequestData[PutCall[A], Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PutCall[A]](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }

  implicit def putWithBodyCompiler[Bd, A] = new ApiCompiler[PutWithBodyCall[Bd, A] :: HNil, FieldType[BodyField.T, Bd] :: HNil] {
    type Out = RequestData[PutWithBodyCall[Bd, A], DataWithBody[Bd]]

    def apply(inputs: FieldType[BodyField.T, Bd] :: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PutWithBodyCall[Bd, A]](uri.result() :: queries :: headers :: inputs.head :: HNil) :: HNil
    }
  }

  implicit def postCompiler[A] = new ApiCompiler[PostCall[A] :: HNil, HNil] {
    type Out = RequestData[PostCall[A], Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PostCall[A]](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }

  implicit def postWithBodyCompiler[Bd, A] = new ApiCompiler[PostWithBodyCall[Bd, A] :: HNil, FieldType[BodyField.T, Bd] :: HNil] {
    type Out = RequestData[PostWithBodyCall[Bd, A], DataWithBody[Bd]]

    def apply(inputs: FieldType[BodyField.T, Bd] :: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PostWithBodyCall[Bd, A]](uri.result() :: queries :: headers :: inputs.head :: HNil) :: HNil
    }
  }

  implicit def deleteCompiler[A] = new ApiCompiler[DeleteCall[A] :: HNil, HNil] {
    type Out = RequestData[DeleteCall[A], Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[DeleteCall[A]](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }
}

trait ApiCompilerMediumPrio extends ApiCompilerLowPrio {

  implicit def queryListInputCompiler[T <: HList, K <: Symbol, V, In <: HList](implicit wit: Witness.Aux[K], compiler: ApiCompiler[T, In]) = new ApiCompiler[QueryInput :: T, FieldType[K, List[V]] :: In] {
    type Out = compiler.Out

    def apply(inputs: FieldType[K, List[V]] :: In, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val queryName           = wit.value.name
      val queryValue: List[V] = inputs.head

      compiler(inputs.tail, uri, Map((queryName, queryValue.map(_.toString()))) ++ queries, headers)
    }
  }
}

trait ApiCompilerList[H <: HList] {

  type Out <: HList

  def compilers: Out
}

object ApiCompilerList {

  type Aux[H <: HList, Out0 <: HList] = ApiCompilerList[H] { type Out = Out0 }
}

trait ApiCompilerListLowPrio {

  implicit def lastCompilerList[El0 <: HList, In0 <: HList, D0 <: HList](implicit compiler0: ApiCompiler.Aux[El0, In0, D0]) = new ApiCompilerList[Transformed[El0, In0, D0] :: HNil] {
    type Out = ApiCompiler.Aux[El0, In0, D0] :: HNil

    val compilers = compiler0 :: HNil
  }

  implicit def compilerList[El0 <: HList, In0 <: HList, D0 <: HList, T <: HList](implicit compiler0: ApiCompiler.Aux[El0, In0, D0], list: ApiCompilerList[T]) = new ApiCompilerList[Transformed[El0, In0, D0] :: T] {
    type Out = ApiCompiler.Aux[El0, In0, D0] :: list.Out

    val compilers = compiler0 :: list.compilers
  }
}
