package typedapi.shared

import typedapi.Json
import shapeless._
import shapeless.labelled.FieldType

// compilation-only test
final class ApiTransformerSpec extends TypeLevelFoldLeftLowPrio with ApiTransformer {

  case class Foo()

  def testCompile[H <: HList, Out](implicit folder: TypeLevelFoldLeft.Aux[H, Unit, Out]): TypeLevelFoldLeft.Aux[H, Unit, Out] = 
    folder

  val pathW = Witness("test")
  val fooW  = Witness('foo)
  val barW  = Witness('bar)

  testCompile[GetElement[Json, Foo] :: HNil, (HNil, HNil, HNil, GetCall, FieldType[Json, Foo])]
  testCompile[PutElement[Json, Foo] :: HNil, (HNil, HNil, HNil, PutCall, FieldType[Json, Foo])]
  testCompile[PutWithBodyElement[Json, Foo, Json, Foo] :: HNil, (HNil, FieldType[Json, BodyField.T] :: HNil, Foo :: HNil, PutWithBodyCall, FieldType[Json, Foo])]
  testCompile[PostElement[Json, Foo] :: HNil, (HNil, HNil, HNil, PostCall, FieldType[Json, Foo])]
  testCompile[PostWithBodyElement[Json, Foo, Json, Foo] :: HNil, (HNil, FieldType[Json, BodyField.T] :: HNil, Foo :: HNil, PostWithBodyCall, FieldType[Json, Foo])]
  testCompile[DeleteElement[Json, Foo] :: HNil, (HNil, HNil, HNil, DeleteCall, FieldType[Json, Foo])]
  testCompile[GetElement[Json, Foo] :: PathElement[pathW.T] :: HNil, (pathW.T :: HNil, HNil, HNil, GetCall, FieldType[Json, Foo])]
  testCompile[GetElement[Json, Foo] :: SegmentParam[fooW.T, String] :: HNil, (SegmentInput :: HNil, fooW.T :: HNil, String :: HNil, GetCall, FieldType[Json, Foo])]
  testCompile[GetElement[Json, Foo] :: QueryParam[fooW.T, String] :: HNil, (QueryInput :: HNil, fooW.T :: HNil, String :: HNil, GetCall, FieldType[Json, Foo])]
  testCompile[GetElement[Json, Foo] :: QueryParam[fooW.T, List[String]] :: HNil, (QueryInput :: HNil, fooW.T :: HNil, List[String] :: HNil, GetCall, FieldType[Json, Foo])]
  testCompile[GetElement[Json, Foo] :: HeaderParam[fooW.T, String] :: HNil, (HeaderInput :: HNil, fooW.T :: HNil, String :: HNil, GetCall, FieldType[Json, Foo])]
  testCompile[GetElement[Json, Foo] :: FixedHeaderElement[fooW.T, barW.T] :: HNil, (FixedHeader[fooW.T, barW.T] :: HNil, HNil, HNil, GetCall, FieldType[Json, Foo])]
  testCompile[GetElement[Json, Foo] :: ClientHeaderParam[fooW.T, String] :: HNil, (ClientHeaderInput :: HNil, fooW.T :: HNil, String :: HNil, GetCall, FieldType[Json, Foo])]
  testCompile[GetElement[Json, Foo] :: ClientHeaderElement[fooW.T, barW.T] :: HNil, (ClientHeader[fooW.T, barW.T] :: HNil, HNil, HNil, GetCall, FieldType[Json, Foo])]
  testCompile[GetElement[Json, Foo] :: ServerHeaderElement[fooW.T, barW.T] :: HNil, (ServerHeader[fooW.T, barW.T] :: HNil, HNil, HNil, GetCall, FieldType[Json, Foo])]

  testCompile[
    GetElement[Json, Foo] :: HeaderParam[fooW.T, Boolean] :: QueryParam[fooW.T, Int] :: SegmentParam[fooW.T, String] :: PathElement[pathW.T] :: HNil, 
    (pathW.T :: SegmentInput :: QueryInput :: HeaderInput :: HNil, fooW.T :: fooW.T :: fooW.T :: HNil, String :: Int :: Boolean :: HNil, GetCall, FieldType[Json, Foo])
  ]
}
