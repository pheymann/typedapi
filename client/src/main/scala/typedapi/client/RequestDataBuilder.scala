package typedapi.client

import typedapi.shared._
import shapeless._
import shapeless.labelled.FieldType

import scala.collection.mutable.Builder
import scala.annotation.implicitNotFound

/** Compiles type level api description into a function returning data (uri, query, header, body) and return-type `A` which are used for a request. */
@implicitNotFound("""Woops, you shouldn't be here. We cannot find the RequestDataBuilder. This seems to be a bug.

elements: ${El}
input keys: ${KIn}
input values: ${VIn}
method: ${M}
expected result: ${O}""")
trait RequestDataBuilder[El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, O] {

  type Out <: HList

  def apply(inputs: VIn, 
            uri: Builder[String, List[String]], 
            queries: Map[String, List[String]], 
            headers: Map[String, String]): Out
}

object RequestDataBuilder extends RequestDataBuilderMediumPrio {

  type Aux[El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, O, Out0 <: HList] = RequestDataBuilder[El, KIn, VIn, M, O] { type Out = Out0 }
}

trait RequestDataBuilderLowPrio {

  implicit def pathCompiler[S, T <: HList, KIn <: HList, VIn <: HList, M <: MethodType, O](implicit wit: Witness.Aux[S], compiler: RequestDataBuilder[T, KIn, VIn, M, O]) = 
    new RequestDataBuilder[S :: T, KIn, VIn, M, O] {
      type Out = compiler.Out

      def apply(inputs: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        compiler(inputs, uri += wit.value.toString(), queries, headers)
      }
    }
  
  implicit def segmentInputCompiler[T <: HList, K, V, KIn <: HList, VIn <: HList, M <: MethodType, O](implicit compiler: RequestDataBuilder[T, KIn, VIn, M, O]) = 
    new RequestDataBuilder[SegmentInput :: T, K :: KIn, V :: VIn, M, O] {
      type Out = compiler.Out

      def apply(inputs: V :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val segValue = inputs.head

        compiler(inputs.tail, uri += segValue.toString(), queries, headers)
      }
    }

  implicit def queryInputCompiler[T <: HList, K, V, KIn <: HList, VIn <: HList, M <: MethodType, O]
      (implicit wit: Witness.Aux[K], show: WitnessToString[K], compiler: RequestDataBuilder[T, KIn, VIn, M, O]) =
    new RequestDataBuilder[QueryInput :: T, K :: KIn, V :: VIn, M, O] {
      type Out = compiler.Out

      def apply(inputs: V :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val queryName  = show.show(wit.value)
        val queryValue = inputs.head

        compiler(inputs.tail, uri, Map((queryName, List(queryValue.toString()))) ++ queries, headers)
      }
    }

  implicit def headerInputCompiler[T <: HList, K, V, KIn <: HList, VIn <: HList, M <: MethodType, O]
      (implicit wit: Witness.Aux[K], show: WitnessToString[K], compiler: RequestDataBuilder[T, KIn, VIn, M, O]) =
    new RequestDataBuilder[HeaderInput :: T, K :: KIn, V :: VIn, M, O] {
      type Out = compiler.Out

      def apply(inputs: V :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val headerName  = show.show(wit.value)
        val headerValue = inputs.head

        compiler(inputs.tail, uri, queries, Map((headerName, headerValue.toString())) ++ headers)
      }
    }

  type Data             = List[String] :: Map[String, List[String]] :: Map[String, String] :: HNil
  type DataWithBody[Bd] = List[String] :: Map[String, List[String]] :: Map[String, String] :: Bd :: HNil

  private def accept[MT <: MediaType](headers: Map[String, String], media: MT): Map[String, String] =
    Map(("Accept", media.value)) ++ headers

  private def contentType[MT <: MediaType](headers: Map[String, String], media: MT): Map[String, String] =
    Map(("Content-Type", media.value)) ++ headers

  implicit def getCompiler[MT <: MediaType, A](implicit media: Witness.Aux[MT]) = new RequestDataBuilder[HNil, HNil, HNil, GetCall, FieldType[MT, A]] {
    type Out = Data

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val out = uri.result() :: queries :: accept(headers, media.value) :: HNil

      out
    }
  }

  implicit def putCompiler[MT <: MediaType, A](implicit media: Witness.Aux[MT]) = new RequestDataBuilder[HNil, HNil, HNil, PutCall, FieldType[MT, A]] {
    type Out = Data

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val out = uri.result() :: queries :: accept(headers, media.value) :: HNil

      out
    }
  }

  implicit def putWithBodyCompiler[BMT <: MediaType, Bd, MT <: MediaType, A](implicit bodyMedia: Witness.Aux[BMT], media: Witness.Aux[MT]) = 
    new RequestDataBuilder[HNil, FieldType[BMT, BodyField.T] :: HNil, Bd :: HNil, PutWithBodyCall, FieldType[MT, A]] {
      type Out = DataWithBody[Bd]

      def apply(inputs: Bd :: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val out = uri.result() :: queries :: contentType(accept(headers, media.value), bodyMedia.value) :: inputs.head :: HNil

        out
      }
    }

  implicit def postCompiler[MT <: MediaType, A](implicit media: Witness.Aux[MT]) = new RequestDataBuilder[HNil, HNil, HNil, PostCall, FieldType[MT, A]] {
    type Out = Data

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val out = uri.result() :: queries :: accept(headers, media.value) :: HNil

      out
    }
  }

  implicit def postWithBodyCompiler[BMT <: MediaType, Bd, MT <: MediaType, A](implicit bodyMedia: Witness.Aux[BMT], media: Witness.Aux[MT]) = 
    new RequestDataBuilder[HNil, FieldType[BMT, BodyField.T] :: HNil, Bd :: HNil, PostWithBodyCall, FieldType[MT, A]] {
      type Out = DataWithBody[Bd]

      def apply(inputs: Bd :: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val out = uri.result() :: queries :: contentType(accept(headers, media.value), bodyMedia.value) :: inputs.head :: HNil

        out
      }
    }

  implicit def deleteCompiler[MT <: MediaType, A](implicit media: Witness.Aux[MT]) = new RequestDataBuilder[HNil, HNil, HNil, DeleteCall, FieldType[MT, A]] {
    type Out = Data

    def apply(inputs: HNil, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
      val out = uri.result() :: queries :: accept(headers, media.value) :: HNil

      out
    }
  }
}

trait RequestDataBuilderMediumPrio extends RequestDataBuilderLowPrio {

  implicit def queryOptInputCompiler[T <: HList, K, V, KIn <: HList, VIn <: HList, M <: MethodType, O]
      (implicit wit: Witness.Aux[K], show: WitnessToString[K], compiler: RequestDataBuilder[T, KIn, VIn, M, O]) =
    new RequestDataBuilder[QueryInput :: T, K :: KIn, Option[V] :: VIn, M, O] {
      type Out = compiler.Out

      def apply(inputs: Option[V] :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val queryName      = show.show(wit.value)
        val queryValue     = inputs.head
        val updatedQueries = queryValue.fold(queries)(q => Map(queryName -> List(q.toString())) ++ queries)

        compiler(inputs.tail, uri, updatedQueries, headers)
      }
    }

  implicit def queryListInputCompiler[T <: HList, K, V, KIn <: HList, VIn <: HList, M <: MethodType, O]
      (implicit wit: Witness.Aux[K], show: WitnessToString[K], compiler: RequestDataBuilder[T, KIn, VIn, M, O]) =
    new RequestDataBuilder[QueryInput :: T, K :: KIn, List[V] :: VIn, M, O] {
      type Out = compiler.Out

      def apply(inputs: List[V] :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val queryName  = show.show(wit.value)
        val queryValue = inputs.head

        if (queryValue.isEmpty)
          compiler(inputs.tail, uri, queries, headers)
        else
          compiler(inputs.tail, uri, Map((queryName, queryValue.map(_.toString()))) ++ queries, headers)
      }
    }

  implicit def headersOptInputCompiler[T <: HList, K, V, KIn <: HList, VIn <: HList, M <: MethodType, O]
      (implicit wit: Witness.Aux[K], show: WitnessToString[K], compiler: RequestDataBuilder[T, KIn, VIn, M, O]) =
    new RequestDataBuilder[HeaderInput :: T, K :: KIn, Option[V] :: VIn, M, O] {
      type Out = compiler.Out

      def apply(inputs: Option[V] :: VIn, uri: Builder[String, List[String]], queries: Map[String, List[String]], headers: Map[String, String]): Out = {
        val headerName     = show.show(wit.value)
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

  implicit def lastCompilerList[El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, O, D <: HList](implicit builder: RequestDataBuilder.Aux[El, KIn, VIn, M, O, D]) = 
    new RequestDataBuilderList[(El, KIn, VIn, M, O) :: HNil] {
      type Out = RequestDataBuilder.Aux[El, KIn, VIn, M, O, D] :: HNil

      val builders = builder :: HNil
    }

  implicit def builderList[El <: HList, KIn <: HList, VIn <: HList, M <: MethodType, O, D <: HList, T <: HList](implicit builder: RequestDataBuilder.Aux[El, KIn, VIn, M, O, D], next: RequestDataBuilderList[T]) = 
    new RequestDataBuilderList[(El, KIn, VIn, M, O) :: T] {
      type Out = RequestDataBuilder.Aux[El, KIn, VIn, M, O, D] :: next.Out

      val builders = builder :: next.builders
    }
}

/*
sealed trait FixedHeadersToMap[H <: HList] {

  def value: Map[String, String]
}

object FixedHeadersToMap {

  implicit val baseFixedToMap = new FixedHeadersToMap[HNil] {
    val value = Map.empty
  }

  implicit def inductiveFixedToMap[K, V, T <: HList](implicit key: Witness.Aux[K], 
                                                              keyShow: WitnessToString[K],
                                                              value: Witness.Aux[V], 
                                                              valueShow: WitnessToString[V],
                                                              next: FixedHeadersToMap[T]) = new FixedHeadersToMap[(K, V) :: T] {
    val value = Map((keyShow.show(key), valueShow.show(value))) ++ next.value
  }
}
 */
