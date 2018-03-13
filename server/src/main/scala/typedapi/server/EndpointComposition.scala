package typedapi.server

import shapeless._

sealed trait EndpointComposition

final case class :|:[Fun, T <: EndpointComposition](fun: Fun, tail: T) extends EndpointComposition {

  def :|:[Fun0](fun0: Fun0): Fun0 :|: Fun :|: T = typedapi.server.:|:(fun0, this)
}

case object =: extends EndpointComposition {

  def :|:[Fun](endpoint: Fun) = typedapi.server.:|:(endpoint, this)
}

final class EndpointCompositionDefintion[Comp <: EndpointComposition, Pre <: HList, Out <: HList](precompiled: Pre, merger: EndpointMerger.Aux[Comp, Pre, Out]) {

  def to(compostion: Comp): Out = merger(compostion, precompiled)
}

sealed trait PreCompiledEndpoint[H <: HList] {

  type Comp <: EndpointComposition
  type Pre  <: HList

  def precompiled: Pre
}

object PreCompiledEndpoint {

  type Aux[H <: HList, Comp0 <: EndpointComposition, Pre0 <: HList] = PreCompiledEndpoint[H] {
    type Comp = Comp0
    type Pre  = Pre0
  }
}

trait PreCompiledEndpointLowPrio {

  implicit val hnilCompile = new PreCompiledEndpoint[HNil] {
    type Comp = =:.type
    type Pre  = HNil

    val precompiled = HNil
  }

  implicit def consCompile[H <: HList, El <: HList, In <: HList, ROut, CIn <: HList, Fun, Out, T <: HList](implicit fun: EndpointFunction.Aux[In, CIn, Fun, Out],
                                                                                                                    executor: RouteExtractor.Aux[El, In, HNil, ROut],
                                                                                                                    next: PreCompiledEndpoint[T]) = new PreCompiledEndpoint[(El, In, Out) :: T] {
    type Comp = Fun :|: next.Comp
    type Pre = (EndpointFunction.Aux[In, CIn, Fun, Out], RouteExtractor.Aux[El, In, HNil, ROut]) :: next.Pre

    val precompiled = (fun, executor) :: next.precompiled
  }
}

sealed trait EndpointMerger[Comp <: EndpointComposition, Pre <: HList] {

  type Out <: HList

  def apply(composition: Comp, precompiled: Pre): Out
}

object EndpointMerger {

  type Aux[Comp <: EndpointComposition, Pre <: HList, Out0 <: HList] = EndpointMerger[Comp, Pre] {
    type Out = Out0
  }
}

trait EndpointMergerLowPrio {

  implicit val hnilMerger = new EndpointMerger[=:.type, HNil] {
    type Out = HNil

    def apply(composition: =:.type, precompiled: HNil): HNil = HNil
  }

  implicit def consMerger[El <: HList, In <: HList, ROut, CIn <: HList, Fun, FOut, TC <: EndpointComposition, TP <: HList](implicit next: EndpointMerger[TC, TP]) = 
    new EndpointMerger[Fun :|: TC, (EndpointFunction.Aux[In, CIn, Fun, FOut], RouteExtractor.Aux[El, In, HNil, ROut]) :: TP] {
      type Out = Endpoint[El, In, ROut, CIn, Fun, FOut] :: next.Out

      def apply(composition: Fun :|: TC, precompiled: (EndpointFunction.Aux[In, CIn, Fun, FOut], RouteExtractor.Aux[El, In, HNil, ROut]) :: TP): Out = {
        val (fun, extractor) = precompiled.head
        
        Endpoint(extractor, fun, composition.fun) :: next(composition.tail, precompiled.tail)
      }
    }
}
