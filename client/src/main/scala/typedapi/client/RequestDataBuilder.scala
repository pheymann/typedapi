package typedapi.client

import typedapi.shared._
import shapeless._
import shapeless.labelled.{FieldType, field}

import scala.collection.mutable.Builder
import scala.annotation.implicitNotFound

/** Compiles type level api description into a function returning data (uri, query, header, body) and return-type `A` which are used for a request. */
@implicitNotFound("""Woops, you shouldn't be here. We cannot find the RequestDataBuilder. This seems to be a bug.

elements: ${El}
input keys: ${KIn}
input values: ${VIn}""")
trait RequestDataBuilder[El <: HList, KIn <: HList, VIn <: HList, O] {

  type Out <: HList

  def apply(inputs: VIn, 
            uri: Builder[String, List[String]], 
            queries: Map[String, List[String]], 
            headers: Map[String, String]): Out
}

object RequestDataBuilder extends RequestDataBuilderMediumPrio {

  type Aux[El <: HList, KIn <: HList, VIn <: HList, O, Out0 <: HList] = RequestDataBuilder[El, KIn, VIn, O] { type Out = Out0 }
}

trait RequestDataBuilderLowPrio {

  implicit def pathCompiler[S, T <: HList, KIn <: HList, VIn <: HList, O](implicit wit: Witness.Aux[S], compiler: RequestDataBuilder[T, KIn, VIn, O]) = 
    new RequestDataBuilder[S :: T, KIn, VIn, O] {
      type Out = compiler.Out

      def apply(inputs: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        compiler(inputs, uri += wit.value.toString(), queries, headers)
      }
    }
  
  implicit def segmentInputCompiler[T <: HList, K, V, KIn <: HList, VIn <: HList, O](implicit compiler: RequestDataBuilder[T, KIn, VIn, O]) = 
    new RequestDataBuilder[SegmentInput :: T, K :: KIn, V :: VIn, O] {
      type Out = compiler.Out

      def apply(inputs: V :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val segValue = inputs.head

        compiler(inputs.tail, uri += segValue.toString(), queries, headers)
      }
    }

  implicit def queryInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, O](implicit wit: Witness.Aux[K], compiler: RequestDataBuilder[T, KIn, VIn, O]) = 
    new RequestDataBuilder[QueryInput :: T, K :: KIn, V :: VIn, O] {
      type Out = compiler.Out

      def apply(inputs: V :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val queryName  = wit.value.name
        val queryValue = inputs.head

        compiler(inputs.tail, uri, Map((queryName, List(queryValue.toString()))) ++ queries, headers)
      }
    }

  implicit def headerInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, O](implicit wit: Witness.Aux[K], compiler: RequestDataBuilder[T, KIn, VIn, O]) = 
    new RequestDataBuilder[HeaderInput :: T, K :: KIn, V :: VIn, O] {
      type Out = compiler.Out

      def apply(inputs: V :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val headerName  = wit.value.name
        val headerValue = inputs.head

        compiler(inputs.tail, uri, queries, Map((headerName, headerValue.toString())) ++ headers)
      }
    }

  implicit def rawHeadersInputCompiler[T <: HList, KIn <: HList, VIn <: HList, O](implicit compiler: RequestDataBuilder[T, KIn, VIn, O]) = 
    new RequestDataBuilder[RawHeadersInput :: T, RawHeadersField.T :: KIn, Map[String, String] :: VIn, O] {
      type Out = compiler.Out

      def apply(inputs: Map[String, String] :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val headerMap = inputs.head

        compiler(inputs.tail, uri, queries, headerMap ++ headers)
      }
    }

  type Data                       = List[String] :: Map[String, List[String]] :: Map[String, String] :: HNil
  type DataWithBody[Bd]           = List[String] :: Map[String, List[String]] :: Map[String, String] :: Bd :: HNil
  type RequestData[R, D <: HList] = FieldType[R, D] :: HNil

  implicit def getCompiler[A] = new RequestDataBuilder[GetCall :: HNil, HNil, HNil, A] {
    type Out = RequestData[GetCall, Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[GetCall](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }

  implicit def putCompiler[A] = new RequestDataBuilder[PutCall :: HNil, HNil, HNil, A] {
    type Out = RequestData[PutCall, Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PutCall](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }

  implicit def putWithBodyCompiler[Bd, A] = new RequestDataBuilder[PutWithBodyCall[Bd] :: HNil, BodyField.T :: HNil, Bd :: HNil, A] {
    type Out = RequestData[PutWithBodyCall[Bd], DataWithBody[Bd]]

    def apply(inputs: Bd :: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PutWithBodyCall[Bd]](uri.result() :: queries :: headers :: inputs.head :: HNil) :: HNil
    }
  }

  implicit def postCompiler[A] = new RequestDataBuilder[PostCall :: HNil, HNil, HNil, A] {
    type Out = RequestData[PostCall, Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PostCall](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }

  implicit def postWithBodyCompiler[Bd, A] = new RequestDataBuilder[PostWithBodyCall[Bd] :: HNil, BodyField.T :: HNil, Bd :: HNil, A] {
    type Out = RequestData[PostWithBodyCall[Bd], DataWithBody[Bd]]

    def apply(inputs: Bd :: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[PostWithBodyCall[Bd]](uri.result() :: queries :: headers :: inputs.head :: HNil) :: HNil
    }
  }

  implicit def deleteCompiler[A] = new RequestDataBuilder[DeleteCall :: HNil, HNil, HNil, A] {
    type Out = RequestData[DeleteCall, Data]

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      field[DeleteCall](uri.result() :: queries :: headers :: HNil) :: HNil
    }
  }
}

trait RequestDataBuilderMediumPrio extends RequestDataBuilderLowPrio {

  implicit def queryOptInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, O](implicit wit: Witness.Aux[K], compiler: RequestDataBuilder[T, KIn, VIn, O]) = 
    new RequestDataBuilder[QueryInput :: T, K :: KIn, Option[V] :: VIn, O] {
      type Out = compiler.Out

      def apply(inputs: Option[V] :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val queryName      = wit.value.name
        val queryValue     = inputs.head
        val updatedQueries = queryValue.fold(queries)(q => Map(queryName -> List(q.toString())) ++ queries)

        compiler(inputs.tail, uri, updatedQueries, headers)
      }
    }

  implicit def queryListInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, O](implicit wit: Witness.Aux[K], compiler: RequestDataBuilder[T, KIn, VIn, O]) = 
    new RequestDataBuilder[QueryInput :: T, K :: KIn, List[V] :: VIn, O] {
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

  implicit def headersOptInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, O](implicit wit: Witness.Aux[K], compiler: RequestDataBuilder[T, KIn, VIn, O]) = 
    new RequestDataBuilder[HeaderInput :: T, K :: KIn, Option[V] :: VIn, O] {
      type Out = compiler.Out

      def apply(inputs: Option[V] :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val headerName     = wit.value.name
        val headerValue    = inputs.head
        val updatedHeaders = headerValue.fold(headers)(h => Map(headerName -> h.toString()) ++ headers)

        compiler(inputs.tail, uri, queries, updatedHeaders)
      }
    }
}

@implicitNotFound("""Woops, you shouldn't be here. We cannot find the RequestDataBuilderList. This seems to be a bug.

list: ${H}""")
trait RequestDataBuilderList[H <: HList] {

  type Out <: HList

  def builders: Out
}

object RequestDataBuilderList extends RequestDataBuilderListLowPrio {

  type Aux[H <: HList, Out0 <: HList] = RequestDataBuilderList[H] { type Out = Out0 }
}

trait RequestDataBuilderListLowPrio {

  implicit def lastCompilerList[El <: HList, KIn <: HList, VIn <: HList, O, D <: HList](implicit builder: RequestDataBuilder.Aux[El, KIn, VIn, O, D]) = 
    new RequestDataBuilderList[Transformed[El, KIn, VIn, O, D] :: HNil] {
      type Out = RequestDataBuilder.Aux[El, KIn, VIn, O, D] :: HNil

      val builders = builder :: HNil
    }

  implicit def builderList[El <: HList, KIn <: HList, VIn <: HList, O, D <: HList, T <: HList](implicit builder: RequestDataBuilder.Aux[El, KIn, VIn, O, D], next: RequestDataBuilderList[T]) = 
    new RequestDataBuilderList[Transformed[El, KIn, VIn, O, D] :: T] {
      type Out = RequestDataBuilder.Aux[El, KIn, VIn, O, D] :: next.Out

      val builders = builder :: next.builders
    }
}
