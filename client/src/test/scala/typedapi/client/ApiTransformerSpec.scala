package typedapi.client

import shapeless._
import shapeless.labelled.FieldType

// compilation-only test
final class ApiTransformerSpec {

  case class Foo()

  def test[H <: HList, Out](implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil), Out]): TypeLevelFoldLeft.Aux[H, (HNil, HNil), Out] = 
    folder

  val pathW = Witness("test")
  val fooW  = Witness('foo)

  test[Get[Foo] :: HNil, (GetCall[Foo] :: HNil, HNil)]
  test[Put[Foo] :: HNil, (PutCall[Foo] :: HNil, HNil)]
  test[PutWithBody[Foo, Foo] :: HNil, (PutWithBodyCall[Foo, Foo] :: HNil, FieldType[BodyField.T, Foo] :: HNil)]
  test[Post[Foo] :: HNil, (PostCall[Foo] :: HNil, HNil)]
  test[PostWithBody[Foo, Foo] :: HNil, (PostWithBodyCall[Foo, Foo] :: HNil, FieldType[BodyField.T, Foo] :: HNil)]
  test[Delete[Foo] :: HNil, (DeleteCall[Foo] :: HNil, HNil)]
  test[Path[pathW.T] :: HNil, (pathW.T :: HNil, HNil)]
  test[SegmentParam[fooW.T, String] :: HNil, (SegmentInput :: HNil, FieldType[fooW.T, String] :: HNil)]
  test[QueryParam[fooW.T, String] :: HNil, (QueryInput :: HNil, FieldType[fooW.T, String] :: HNil)]
  test[QueryParam[fooW.T, List[String]] :: HNil, (QueryInput :: HNil, FieldType[fooW.T, List[String]] :: HNil)]
  test[HeaderParam[fooW.T, String] :: HNil, (HeaderInput :: HNil, FieldType[fooW.T, String] :: HNil)]
  test[RawHeaders.type :: HNil, (RawHeadersInput :: HNil, FieldType[RawHeadersField.T, Map[String, String]] :: HNil)]

  test[
    Get[Foo] :: HeaderParam[fooW.T, Boolean] :: QueryParam[fooW.T, Int] :: SegmentParam[fooW.T, String] :: Path[pathW.T] :: HNil, 
    (pathW.T :: SegmentInput :: QueryInput :: HeaderInput :: GetCall[Foo] :: HNil, FieldType[fooW.T, String] :: FieldType[fooW.T, Int] :: FieldType[fooW.T, Boolean] :: HNil)
  ]
}
