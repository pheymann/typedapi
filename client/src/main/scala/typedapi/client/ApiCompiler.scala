package typedapi.client

import typedapi.shared._
import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.collection.mutable.Builder
import scala.annotation.implicitNotFound

/** Compiles type level api description into a function returning data (uri, query, header, body) and return-type `A` which are used for a request. */
@implicitNotFound("""Woops, you shouldn't be here. We cannot find an ApiCompiler.

elements: ${El}
input keys: ${KIn}
input values: ${VIn}""")
trait ApiCompiler[El <: HList, KIn <: HList, VIn <: HList, O] {

  type Out <: HList

  def apply(inputs: VIn, 
            uri: Builder[String, List[String]], 
            queries: Map[String, List[String]], 
            headers: Map[String, String]): Out
}

object ApiCompiler {

  type Aux[El <: HList, KIn <: HList, VIn <: HList, O, Out0 <: HList] = ApiCompiler[El, KIn, VIn, O] { type Out = Out0 }
}

trait ApiCompilerLowPrio {

  implicit def pathCompiler[S, T <: HList, KIn <: HList, VIn <: HList, O](implicit wit: Witness.Aux[S], compiler: ApiCompiler[T, KIn, VIn, O]) = new ApiCompiler[S :: T, KIn, VIn, O] {
    type Out = compiler.Out

    def apply(inputs: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      compiler(inputs, uri += wit.value.toString(), queries, headers)
    }
  }
  
  implicit def segmentInputCompiler[T <: HList, K, V, KIn <: HList, VIn <: HList, O](implicit compiler: ApiCompiler[T, KIn, VIn, O]) = new ApiCompiler[SegmentInput :: T, K :: KIn, V :: VIn, O] {
    type Out = compiler.Out

    def apply(inputs: V :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val segValue = inputs.head

      compiler(inputs.tail, uri += segValue.toString(), queries, headers)
    }
  }

  implicit def queryInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, O](implicit wit: Witness.Aux[K], compiler: ApiCompiler[T, KIn, VIn, O]) = new ApiCompiler[QueryInput :: T, K :: KIn, V :: VIn, O] {
    type Out = compiler.Out

    def apply(inputs: V :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val queryName  = wit.value.name
      val queryValue = inputs.head

      compiler(inputs.tail, uri, Map((queryName, List(queryValue.toString()))) ++ queries, headers)
    }
  }

  implicit def headerInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, O](implicit wit: Witness.Aux[K], compiler: ApiCompiler[T, KIn, VIn, O]) = new ApiCompiler[HeaderInput :: T, K :: KIn, V :: VIn, O] {
    type Out = compiler.Out

    def apply(inputs: V :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val headerName  = wit.value.name
      val headerValue = inputs.head

      compiler(inputs.tail, uri, queries, Map((headerName, headerValue.toString())) ++ headers)
    }
  }

  implicit def rawHeadersInputCompiler[T <: HList, KIn <: HList, VIn <: HList, O](implicit compiler: ApiCompiler[T, KIn, VIn, O]) = new ApiCompiler[RawHeadersInput :: T, RawHeadersField.T :: KIn, Map[String, String] :: VIn, O] {
    type Out = compiler.Out

    def apply(inputs: Map[String, String] :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val headerMap = inputs.head

      compiler(inputs.tail, uri, queries, headerMap ++ headers)
    }
  }

  type Data                       = List[String] :: Map[String, List[String]] :: Map[String, String] :: HNil
  type DataWithBody[Bd]           = List[String] :: Map[String, List[String]] :: Map[String, String] :: Bd :: HNil
  type RequestData[R, D <: HList] = FieldType[R, D] :: HNil

  implicit def getCompiler[A] = new ApiCompiler[GetCall :: HNil, HNil, HNil, A] {
    type Out = RequestData[GetCall, Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[GetCall](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }

  implicit def putCompiler[A] = new ApiCompiler[PutCall :: HNil, HNil, HNil, A] {
    type Out = RequestData[PutCall, Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PutCall](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }

  implicit def putWithBodyCompiler[Bd, A] = new ApiCompiler[PutWithBodyCall[Bd] :: HNil, BodyField.T :: HNil, Bd :: HNil, A] {
    type Out = RequestData[PutWithBodyCall[Bd], DataWithBody[Bd]]

    def apply(inputs: Bd :: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PutWithBodyCall[Bd]](uri.result() :: queries :: headers :: inputs.head :: HNil) :: HNil
    }
  }

  implicit def postCompiler[A] = new ApiCompiler[PostCall :: HNil, HNil, HNil, A] {
    type Out = RequestData[PostCall, Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PostCall](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }

  implicit def postWithBodyCompiler[Bd, A] = new ApiCompiler[PostWithBodyCall[Bd] :: HNil, BodyField.T :: HNil, Bd :: HNil, A] {
    type Out = RequestData[PostWithBodyCall[Bd], DataWithBody[Bd]]

    def apply(inputs: Bd :: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PostWithBodyCall[Bd]](uri.result() :: queries :: headers :: inputs.head :: HNil) :: HNil
    }
  }

  implicit def deleteCompiler[A] = new ApiCompiler[DeleteCall :: HNil, HNil, HNil, A] {
    type Out = RequestData[DeleteCall, Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[DeleteCall](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }
}

trait ApiCompilerMediumPrio extends ApiCompilerLowPrio {

  implicit def queryOptInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, O](implicit wit: Witness.Aux[K], compiler: ApiCompiler[T, KIn, VIn, O]) = new ApiCompiler[QueryInput :: T, K :: KIn, Option[V] :: VIn, O] {
    type Out = compiler.Out

    def apply(inputs: Option[V] :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val queryName      = wit.value.name
      val queryValue     = inputs.head
      val updatedQueries = queryValue.fold(queries)(q => Map(queryName -> List(q.toString())) ++ queries)

      compiler(inputs.tail, uri, updatedQueries, headers)
    }
  }

  implicit def queryListInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, O](implicit wit: Witness.Aux[K], compiler: ApiCompiler[T, KIn, VIn, O]) = new ApiCompiler[QueryInput :: T, K :: KIn, List[V] :: VIn, O] {
    type Out = compiler.Out

    def apply(inputs: List[V] :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val queryName  = wit.value.name
      val queryValue = inputs.head

      if (queryValue.isEmpty)
        compiler(inputs.tail, uri, queries, headers)
      else
        compiler(inputs.tail, uri, Map((queryName, queryValue.map(_.toString()))) ++ queries, headers)
    }
  }

  implicit def headersOptInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, O](implicit wit: Witness.Aux[K], compiler: ApiCompiler[T, KIn, VIn, O]) = new ApiCompiler[HeaderInput :: T, K :: KIn, Option[V] :: VIn, O] {
    type Out = compiler.Out

    def apply(inputs: Option[V] :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val headerName     = wit.value.name
      val headerValue    = inputs.head
      val updatedHeaders = headerValue.fold(headers)(h => Map(headerName -> h.toString()) ++ headers)

      compiler(inputs.tail, uri, queries, updatedHeaders)
    }
  }
}

@implicitNotFound("""Woops, you shouldn't be here. We cannot find an ApiCompilerList.

list: ${H}""")
trait ApiCompilerList[H <: HList] {

  type Out <: HList

  def compilers: Out
}

object ApiCompilerList {

  type Aux[H <: HList, Out0 <: HList] = ApiCompilerList[H] { type Out = Out0 }
}

trait ApiCompilerListLowPrio {

  implicit def lastCompilerList[El <: HList, KIn <: HList, VIn <: HList, O, D <: HList](implicit compiler: ApiCompiler.Aux[El, KIn, VIn, O, D]) = new ApiCompilerList[Transformed[El, KIn, VIn, O, D] :: HNil] {
    type Out = ApiCompiler.Aux[El, KIn, VIn, O, D] :: HNil

    val compilers = compiler :: HNil
  }

  implicit def compilerList[El <: HList, KIn <: HList, VIn <: HList, O, D <: HList, T <: HList](implicit compiler: ApiCompiler.Aux[El, KIn, VIn, O, D], next: ApiCompilerList[T]) = 
    new ApiCompilerList[Transformed[El, KIn, VIn, O, D] :: T] {
      type Out = ApiCompiler.Aux[El, KIn, VIn, O, D] :: next.Out

      val compilers = compiler :: next.compilers
    }
}
