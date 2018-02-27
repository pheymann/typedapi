package typedapi.client

import shapeless._
import shapeless.labelled.FieldType

// compilation-only test
final class ApiTransformerSpec {

  case class Foo()

  def test[El <: HList, In <: HList](transformed: (ElementTypes[El], InputTypes[In])) = transformed

  val testW = Witness("test")
  val fooW  = Witness('foo)
  val base  = := :> "test"

  test[testW.T :: GetCall[Foo] :: HNil, HNil](transform(base :> Get[Foo]))
  test[testW.T :: PutCall[Foo] :: HNil, HNil](transform(base :> Put[Foo]))
  test[testW.T :: PutWithBodyCall[Foo, Foo] :: HNil, FieldType[BodyField.T, Foo] :: HNil](
    transform(base :> ReqBody[Foo] :> Put[Foo])
  )
  test[testW.T :: PostCall[Foo] :: HNil, HNil](transform(base :> Post[Foo]))
  test[testW.T :: PostWithBodyCall[Foo, Foo] :: HNil, FieldType[BodyField.T, Foo] :: HNil](
    transform(base :> ReqBody[Foo] :> Post[Foo])
  )
  test[testW.T :: DeleteCall[Foo] :: HNil, HNil](transform(base :> Delete[Foo]))

  test[testW.T :: SegmentInput :: GetCall[Foo] :: HNil, FieldType[fooW.T, Int] :: HNil](
    transform(base :> Segment[Int](fooW) :> Get[Foo])
  )
  test[testW.T :: QueryInput :: GetCall[Foo] :: HNil, FieldType[fooW.T, Int] :: HNil](
    transform(base :> Query[Int](fooW) :> Get[Foo])
  )
  test[testW.T :: QueryInput :: GetCall[Foo] :: HNil, FieldType[fooW.T, List[Int]] :: HNil](
    transform(base :> Query[List[Int]](fooW) :> Get[Foo])
  )
  test[testW.T :: HeaderInput :: GetCall[Foo] :: HNil, FieldType[fooW.T, Int] :: HNil](
    transform(base :> Header[Int](fooW) :> Get[Foo])
  )
  test[testW.T :: RawHeadersInput :: GetCall[Foo] :: HNil, FieldType[RawHeadersField.T, Map[String, String]] :: HNil](
    transform(base :> RawHeaders :> Get[Foo])
  )

  transform(
    := :> "test" :> "test2" :> Segment[Int]('foo) :> "test3" :> Query[Int]('foo2) :> Header[Int]('foo3) :> Get[Foo]
  )

  // none final ApiList
  shapeless.test.illTyped("test[testW.T :: GetCall[Foo] :: HNil, HNil](transform(base))")
}
