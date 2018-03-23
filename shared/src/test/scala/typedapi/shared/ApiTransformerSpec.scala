package typedapi.shared

import shapeless._
import shapeless.labelled.FieldType

// compilation-only test
final class ApiTransformerSpec extends TypeLevelFoldLeftLowPrio with ApiTransformer with ops.ApiListOps {

  case class Foo()

  def test[H <: HList, Out](implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil), Out]): TypeLevelFoldLeft.Aux[H, (HNil, HNil), Out] = 
    folder

  val pathW = Witness("test")
  val fooW  = Witness('foo)

  test[GetElement[Foo] :: HNil, (GetCall :: HNil, HNil, Foo)]
  test[PutElement[Foo] :: HNil, (PutCall :: HNil, HNil, Foo)]
  test[PutWithBodyElement[Foo, Foo] :: HNil, (PutWithBodyCall[Foo] :: HNil, FieldType[BodyField.T, Foo] :: HNil, Foo)]
  test[PostElement[Foo] :: HNil, (PostCall :: HNil, HNil, Foo)]
  test[PostWithBodyElement[Foo, Foo] :: HNil, (PostWithBodyCall[Foo] :: HNil, FieldType[BodyField.T, Foo] :: HNil, Foo)]
  test[DeleteElement[Foo] :: HNil, (DeleteCall :: HNil, HNil, Foo)]
  test[GetElement[Foo] :: PathElement[pathW.T] :: HNil, (pathW.T :: GetCall :: HNil, HNil, Foo)]
  test[GetElement[Foo] :: SegmentParam[fooW.T, String] :: HNil, (SegmentInput :: GetCall :: HNil, FieldType[fooW.T, String] :: HNil, Foo)]
  test[GetElement[Foo] :: QueryParam[fooW.T, String] :: HNil, (QueryInput :: GetCall :: HNil, FieldType[fooW.T, String] :: HNil, Foo)]
  test[GetElement[Foo] :: QueryParam[fooW.T, List[String]] :: HNil, (QueryInput :: GetCall :: HNil, FieldType[fooW.T, List[String]] :: HNil, Foo)]
  test[GetElement[Foo] :: HeaderParam[fooW.T, String] :: HNil, (HeaderInput :: GetCall :: HNil, FieldType[fooW.T, String] :: HNil, Foo)]
  test[GetElement[Foo] :: RawHeaders.type :: HNil, (RawHeadersInput :: GetCall :: HNil, FieldType[RawHeadersField.T, Map[String, String]] :: HNil, Foo)]

  test[
    GetElement[Foo] :: HeaderParam[fooW.T, Boolean] :: QueryParam[fooW.T, Int] :: SegmentParam[fooW.T, String] :: PathElement[pathW.T] :: HNil, 
    (pathW.T :: SegmentInput :: QueryInput :: HeaderInput :: GetCall :: HNil, FieldType[fooW.T, String] :: FieldType[fooW.T, Int] :: FieldType[fooW.T, Boolean] :: HNil, Foo)
  ]
}
