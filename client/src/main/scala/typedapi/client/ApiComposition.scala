package typedapi.client

import shapeless._

/** Syntactic sugar to enable:
  *   val api = (:= "foo" :> Get[Foo]) :|: (:= "bar" :> Get[Bar])
  * 
  *   val (foo :|: bar :|: :=) = compile(transform(api))
  */
sealed trait ApiComposition

final case class :|:[El <: HList, In <: HList, O, D <: HList, T <: ApiComposition](compiler: ApiCompiler.Aux[El, In, O, D], tail: T) extends ApiComposition

case object =: extends ApiComposition {

  def :|:[El <: HList, In <: HList, O, D <: HList](compiler: ApiCompiler.Aux[El, In, O, D]) = typedapi.client.:|:(compiler, this)
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

  implicit def consComposition[El <: HList, In <: HList, O, D <: HList, T <: HList](implicit next: HListToComposition[T]) = new HListToComposition[ApiCompiler.Aux[El, In, O, D] :: T] {

    type Out = :|:[El, In, O, D, next.Out]

    def apply(h: ApiCompiler.Aux[El, In, O, D] :: T): Out = :|:(h.head, next(h.tail))
  }
}
