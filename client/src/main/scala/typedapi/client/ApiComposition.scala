package typedapi.client

import shapeless._
import shapeless.ops.function.FnFromProduct

/** Syntactic sugar to enable:
  *   val api = (:= "foo" :> Get[Foo]) :|: (:= "bar" :> Get[Bar])
  * 
  *   val (foo :|: bar :|: =:) = compile(transform(api))
  */
sealed trait ApiComposition

final case class :|:[Fn, T <: ApiComposition](fn: Fn, tail: T) extends ApiComposition

case object =: extends ApiComposition {

  def :|:[Fn](fn: Fn) = typedapi.client.:|:(fn, this)
}

/** Transform apis composed in a HList into ApiComposition representation. */
sealed trait HListToComposition[H <: HList] {

  type Out <: ApiComposition

  def apply(h: H): Out
}

trait HListToCompositionLowPrio {

  implicit val hnilComposition = new HListToComposition[HNil] {
    type Out = =:.type

    def apply(h: HNil): Out = =:
  }

  implicit def consComposition[El <: HList, KIn <: HList, VIn <: HList, O, D <: HList, T <: HList](implicit next: HListToComposition[T],
                                                                                                            inToFn: FnFromProduct[VIn => ExecutableDerivation[El, KIn, VIn, O, D]]) = 
    new HListToComposition[ApiCompiler.Aux[El, KIn, VIn, O, D] :: T] {
      type Out = :|:[inToFn.Out, next.Out]

      def apply(comps: ApiCompiler.Aux[El, KIn, VIn, O, D] :: T): Out = {
        val fn = inToFn.apply(input => new ExecutableDerivation[El, KIn, VIn, O, D](comps.head, input))

        :|:(fn, next(comps.tail))
      }
    }
}
