package typedapi.client

import typedapi.shared._
import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.collection.mutable.Builder
import scala.annotation.implicitNotFound

/** Compiles type level api description into a function returning data (uri, query, header, body) and return-type `A` which are used for a request. */
@implicitNotFound("Something went really wrong, cannot find ApiCompiler." +
                  "  elements: ${El}\n  inputs: ${In}")
trait ApiCompiler[El <: HList, In <: HList, O] {

  type Out <: HList

  def apply(inputs: In, 
            uri: Builder[String, List[String]], 
            queries: Map[String, List[String]], 
            headers: Map[String, String]): Out
}

object ApiCompiler {

  type Aux[El <: HList, In <: HList, O, Out0 <: HList] = ApiCompiler[El, In, O] { type Out = Out0 }
}

trait ApiCompilerLowPrio {

  implicit def pathCompiler[S, T <: HList, In <: HList, O](implicit wit: Witness.Aux[S], compiler: ApiCompiler[T, In, O]) = new ApiCompiler[S :: T, In, O] {
    type Out = compiler.Out

    def apply(inputs: In, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      compiler(inputs, uri += wit.value.toString(), queries, headers)
    }
  }
  
  implicit def segmentInputCompiler[T <: HList, K, V, In <: HList, O](implicit compiler: ApiCompiler[T, In, O]) = new ApiCompiler[SegmentInput :: T, FieldType[K, V] :: In, O] {
    type Out = compiler.Out

    def apply(inputs: FieldType[K, V] :: In, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val segValue: V = inputs.head

      compiler(inputs.tail, uri += segValue.toString(), queries, headers)
    }
  }

  implicit def queryInputCompiler[T <: HList, K <: Symbol, V, In <: HList, O](implicit wit: Witness.Aux[K], compiler: ApiCompiler[T, In, O]) = new ApiCompiler[QueryInput :: T, FieldType[K, V] :: In, O] {
    type Out = compiler.Out

    def apply(inputs: FieldType[K, V] :: In, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val queryName     = wit.value.name
      val queryValue: V = inputs.head

      compiler(inputs.tail, uri, Map((queryName, List(queryValue.toString()))) ++ queries, headers)
    }
  }

  implicit def headerInputCompiler[T <: HList, K <: Symbol, V, In <: HList, O](implicit wit: Witness.Aux[K], compiler: ApiCompiler[T, In, O]) = new ApiCompiler[HeaderInput :: T, FieldType[K, V] :: In, O] {
    type Out = compiler.Out

    def apply(inputs: FieldType[K, V] :: In, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val headerName     = wit.value.name
      val headerValue: V = inputs.head

      compiler(inputs.tail, uri, queries, Map((headerName, headerValue.toString())) ++ headers)
    }
  }

  implicit def rawHeadersInputCompiler[T <: HList, In <: HList, O](implicit compiler: ApiCompiler[T, In, O]) = new ApiCompiler[RawHeadersInput :: T, FieldType[RawHeadersField.T, Map[String, String]] :: In, O] {
    type Out = compiler.Out

    def apply(inputs: FieldType[RawHeadersField.T, Map[String, String]] :: In, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val headerMap: Map[String, String] = inputs.head

      compiler(inputs.tail, uri, queries, headerMap ++ headers)
    }
  }

  type Data                       = List[String] :: Map[String, List[String]] :: Map[String, String] :: HNil
  type DataWithBody[Bd]           = List[String] :: Map[String, List[String]] :: Map[String, String] :: Bd :: HNil
  type RequestData[R, D <: HList] = FieldType[R, D] :: HNil

  implicit def getCompiler[A] = new ApiCompiler[GetCall :: HNil, HNil, A] {
    type Out = RequestData[GetCall, Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[GetCall](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }

  implicit def putCompiler[A] = new ApiCompiler[PutCall :: HNil, HNil, A] {
    type Out = RequestData[PutCall, Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PutCall](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }

  implicit def putWithBodyCompiler[Bd, A] = new ApiCompiler[PutWithBodyCall[Bd] :: HNil, FieldType[BodyField.T, Bd] :: HNil, A] {
    type Out = RequestData[PutWithBodyCall[Bd], DataWithBody[Bd]]

    def apply(inputs: FieldType[BodyField.T, Bd] :: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PutWithBodyCall[Bd]](uri.result() :: queries :: headers :: inputs.head :: HNil) :: HNil
    }
  }

  implicit def postCompiler[A] = new ApiCompiler[PostCall :: HNil, HNil, A] {
    type Out = RequestData[PostCall, Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PostCall](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }

  implicit def postWithBodyCompiler[Bd, A] = new ApiCompiler[PostWithBodyCall[Bd] :: HNil, FieldType[BodyField.T, Bd] :: HNil, A] {
    type Out = RequestData[PostWithBodyCall[Bd], DataWithBody[Bd]]

    def apply(inputs: FieldType[BodyField.T, Bd] :: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PostWithBodyCall[Bd]](uri.result() :: queries :: headers :: inputs.head :: HNil) :: HNil
    }
  }

  implicit def deleteCompiler[A] = new ApiCompiler[DeleteCall :: HNil, HNil, A] {
    type Out = RequestData[DeleteCall, Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[DeleteCall](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }
}

trait ApiCompilerMediumPrio extends ApiCompilerLowPrio {

  implicit def queryOptInputCompiler[T <: HList, K <: Symbol, V, In <: HList, O](implicit wit: Witness.Aux[K], compiler: ApiCompiler[T, In, O]) = new ApiCompiler[QueryInput :: T, FieldType[K, Option[V]] :: In, O] {
    type Out = compiler.Out

    def apply(inputs: FieldType[K, Option[V]] :: In, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val queryName             = wit.value.name
      val queryValue: Option[V] = inputs.head
      val updatedQueries        = queryValue.fold(queries)(q => Map(queryName -> List(q.toString())) ++ queries)

      compiler(inputs.tail, uri, updatedQueries, headers)
    }
  }

  implicit def queryListInputCompiler[T <: HList, K <: Symbol, V, In <: HList, O](implicit wit: Witness.Aux[K], compiler: ApiCompiler[T, In, O]) = new ApiCompiler[QueryInput :: T, FieldType[K, List[V]] :: In, O] {
    type Out = compiler.Out

    def apply(inputs: FieldType[K, List[V]] :: In, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val queryName           = wit.value.name
      val queryValue: List[V] = inputs.head

      if (queryValue.isEmpty)
        compiler(inputs.tail, uri, queries, headers)
      else
        compiler(inputs.tail, uri, Map((queryName, queryValue.map(_.toString()))) ++ queries, headers)
    }
  }

  implicit def headersOptInputCompiler[T <: HList, K <: Symbol, V, In <: HList, O](implicit wit: Witness.Aux[K], compiler: ApiCompiler[T, In, O]) = new ApiCompiler[HeaderInput :: T, FieldType[K, Option[V]] :: In, O] {
    type Out = compiler.Out

    def apply(inputs: FieldType[K, Option[V]] :: In, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val headerName             = wit.value.name
      val headerValue: Option[V] = inputs.head
      val updatedHeaders         = headerValue.fold(headers)(h => Map(headerName -> h.toString()) ++ headers)

      compiler(inputs.tail, uri, queries, updatedHeaders)
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

  implicit def lastCompilerList[El0 <: HList, In0 <: HList, O, D0 <: HList](implicit compiler0: ApiCompiler.Aux[El0, In0, O, D0]) = new ApiCompilerList[Transformed[El0, In0, O, D0] :: HNil] {
    type Out = ApiCompiler.Aux[El0, In0, O, D0] :: HNil

    val compilers = compiler0 :: HNil
  }

  implicit def compilerList[El0 <: HList, In0 <: HList, O, D0 <: HList, T <: HList](implicit compiler0: ApiCompiler.Aux[El0, In0, O, D0], list: ApiCompilerList[T]) = new ApiCompilerList[Transformed[El0, In0, O, D0] :: T] {
    type Out = ApiCompiler.Aux[El0, In0, O, D0] :: list.Out

    val compilers = compiler0 :: list.compilers
  }
}
