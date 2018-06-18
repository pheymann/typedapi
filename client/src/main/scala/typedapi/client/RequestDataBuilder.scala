package typedapi.client

import typedapi.shared._
import shapeless._

import scala.collection.mutable.Builder
import scala.annotation.implicitNotFound

/** Compiles type level api description into a function returning data (uri, query, header, body) and return-type `A` which are used for a request. */
@implicitNotFound("""Woops, you shouldn't be here. We cannot find the RequestDataBuilder. This seems to be a bug.

elements: ${El}
input keys: ${KIn}
input values: ${VIn}
method: ${M}
expected result: ${O}""")
trait RequestDataBuilder[El <: HList, KIn <: HList, VIn <: HList, M <: MethodCall, O] {

  type Out <: HList

  def apply(inputs: VIn, 
            uri: Builder[String, List[String]], 
            queries: Map[String, List[String]], 
            headers: Map[String, String]): Out
}

object RequestDataBuilder extends RequestDataBuilderMediumPrio {

  type Aux[El <: HList, KIn <: HList, VIn <: HList, M <: MethodCall, O, Out0 <: HList] = RequestDataBuilder[El, KIn, VIn, M, O] { type Out = Out0 }
}

trait RequestDataBuilderLowPrio {

  implicit def pathCompiler[S, T <: HList, KIn <: HList, VIn <: HList, M <: MethodCall, O](implicit wit: Witness.Aux[S], compiler: RequestDataBuilder[T, KIn, VIn, M, O]) = 
    new RequestDataBuilder[S :: T, KIn, VIn, M, O] {
      type Out = compiler.Out

      def apply(inputs: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        compiler(inputs, uri += wit.value.toString(), queries, headers)
      }
    }
  
  implicit def segmentInputCompiler[T <: HList, K, V, KIn <: HList, VIn <: HList, M <: MethodCall, O](implicit compiler: RequestDataBuilder[T, KIn, VIn, M, O]) = 
    new RequestDataBuilder[SegmentInput :: T, K :: KIn, V :: VIn, M, O] {
      type Out = compiler.Out

      def apply(inputs: V :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val segValue = inputs.head

        compiler(inputs.tail, uri += segValue.toString(), queries, headers)
      }
    }

  implicit def queryInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, M <: MethodCall, O](implicit wit: Witness.Aux[K], compiler: RequestDataBuilder[T, KIn, VIn, M, O]) = 
    new RequestDataBuilder[QueryInput :: T, K :: KIn, V :: VIn, M, O] {
      type Out = compiler.Out

      def apply(inputs: V :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val queryName  = wit.value.name
        val queryValue = inputs.head

        compiler(inputs.tail, uri, Map((queryName, List(queryValue.toString()))) ++ queries, headers)
      }
    }

  implicit def headerInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, M <: MethodCall, O](implicit wit: Witness.Aux[K], compiler: RequestDataBuilder[T, KIn, VIn, M, O]) = 
    new RequestDataBuilder[HeaderInput :: T, K :: KIn, V :: VIn, M, O] {
      type Out = compiler.Out

      def apply(inputs: V :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val headerName  = wit.value.name
        val headerValue = inputs.head

        compiler(inputs.tail, uri, queries, Map((headerName, headerValue.toString())) ++ headers)
      }
    }

  implicit def rawHeadersInputCompiler[T <: HList, KIn <: HList, VIn <: HList, M <: MethodCall, O](implicit compiler: RequestDataBuilder[T, KIn, VIn, M, O]) = 
    new RequestDataBuilder[RawHeadersInput :: T, RawHeadersField.T :: KIn, Map[String, String] :: VIn, M, O] {
      type Out = compiler.Out

      def apply(inputs: Map[String, String] :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val headerMap = inputs.head

        compiler(inputs.tail, uri, queries, headerMap ++ headers)
      }
    }

  type Data             = List[String] :: Map[String, List[String]] :: Map[String, String] :: HNil
  type DataWithBody[Bd] = List[String] :: Map[String, List[String]] :: Map[String, String] :: Bd :: HNil

  implicit def getCompiler[A] = new RequestDataBuilder[HNil, HNil, HNil, GetCall, A] {
    type Out = Data

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val out = uri.result() :: queries :: headers :: HNil

      out
    }
  }

  implicit def putCompiler[A] = new RequestDataBuilder[HNil, HNil, HNil, PutCall, A] {
    type Out = Data

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val out = uri.result() :: queries :: headers :: HNil

      out
    }
  }

  implicit def putWithBodyCompiler[Bd, A] = new RequestDataBuilder[HNil, BodyField.T :: HNil, Bd :: HNil, PutWithBodyCall, A] {
    type Out = DataWithBody[Bd]

    def apply(inputs: Bd :: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val out = uri.result() :: queries :: headers :: inputs.head :: HNil

      out
    }
  }

  implicit def postCompiler[A] = new RequestDataBuilder[HNil, HNil, HNil, PostCall, A] {
    type Out = Data

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val out = uri.result() :: queries :: headers :: HNil

      out
    }
  }

  implicit def postWithBodyCompiler[Bd, A] = new RequestDataBuilder[HNil, BodyField.T :: HNil, Bd :: HNil, PostWithBodyCall, A] {
    type Out = DataWithBody[Bd]

    def apply(inputs: Bd :: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val out = uri.result() :: queries :: headers :: inputs.head :: HNil

      out
    }
  }

  implicit def deleteCompiler[A] = new RequestDataBuilder[HNil, HNil, HNil, DeleteCall, A] {
    type Out = Data

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val out = uri.result() :: queries :: headers :: HNil

      out
    }
  }
}

trait RequestDataBuilderMediumPrio extends RequestDataBuilderLowPrio {

  implicit def queryOptInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, M <: MethodCall, O](implicit wit: Witness.Aux[K], compiler: RequestDataBuilder[T, KIn, VIn, M, O]) = 
    new RequestDataBuilder[QueryInput :: T, K :: KIn, Option[V] :: VIn, M, O] {
      type Out = compiler.Out

      def apply(inputs: Option[V] :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val queryName      = wit.value.name
        val queryValue     = inputs.head
        val updatedQueries = queryValue.fold(queries)(q => Map(queryName -> List(q.toString())) ++ queries)

        compiler(inputs.tail, uri, updatedQueries, headers)
      }
    }

  implicit def queryListInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, M <: MethodCall, O](implicit wit: Witness.Aux[K], compiler: RequestDataBuilder[T, KIn, VIn, M, O]) = 
    new RequestDataBuilder[QueryInput :: T, K :: KIn, List[V] :: VIn, M, O] {
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

  implicit def headersOptInputCompiler[T <: HList, K <: Symbol, V, KIn <: HList, VIn <: HList, M <: MethodCall, O](implicit wit: Witness.Aux[K], compiler: RequestDataBuilder[T, KIn, VIn, M, O]) = 
    new RequestDataBuilder[HeaderInput :: T, K :: KIn, Option[V] :: VIn, M, O] {
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

  implicit def lastCompilerList[El <: HList, KIn <: HList, VIn <: HList, M <: MethodCall, O, D <: HList](implicit builder: RequestDataBuilder.Aux[El, KIn, VIn, M, O, D]) = 
    new RequestDataBuilderList[(El, KIn, VIn, M, O) :: HNil] {
      type Out = RequestDataBuilder.Aux[El, KIn, VIn, M, O, D] :: HNil

      val builders = builder :: HNil
    }

  implicit def builderList[El <: HList, KIn <: HList, VIn <: HList, M <: MethodCall, O, D <: HList, T <: HList](implicit builder: RequestDataBuilder.Aux[El, KIn, VIn, M, O, D], next: RequestDataBuilderList[T]) = 
    new RequestDataBuilderList[(El, KIn, VIn, M, O) :: T] {
      type Out = RequestDataBuilder.Aux[El, KIn, VIn, M, O, D] :: next.Out

      val builders = builder :: next.builders
    }
}
