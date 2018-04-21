package typedapi.shared

import shapeless._

// compilation-only test
final class ApiTransformerSpec extends TypeLevelFoldLeftLowPrio with ApiTransformer {

  case class Foo()

  def testCompile[H <: HList, Out](implicit folder: TypeLevelFoldLeft.Aux[H, (HNil, HNil, HNil), Out]): TypeLevelFoldLeft.Aux[H, (HNil, HNil, HNil), Out] = 
    folder

  val pathW = Witness("test")
  val fooW  = Witness('foo)

  testCompile[GetElement[Foo] :: HNil, (GetCall :: HNil, HNil, HNil, Foo)]
  testCompile[PutElement[Foo] :: HNil, (PutCall :: HNil, HNil, HNil, Foo)]
  testCompile[PutWithBodyElement[Foo, Foo] :: HNil, (PutWithBodyCall[Foo] :: HNil, BodyField.T :: HNil, Foo :: HNil, Foo)]
  testCompile[PostElement[Foo] :: HNil, (PostCall :: HNil, HNil, HNil, Foo)]
  testCompile[PostWithBodyElement[Foo, Foo] :: HNil, (PostWithBodyCall[Foo] :: HNil, BodyField.T :: HNil, Foo :: HNil, Foo)]
  testCompile[DeleteElement[Foo] :: HNil, (DeleteCall :: HNil, HNil, HNil, Foo)]
  testCompile[GetElement[Foo] :: PathElement[pathW.T] :: HNil, (pathW.T :: GetCall :: HNil, HNil, HNil, Foo)]
  testCompile[GetElement[Foo] :: SegmentParam[fooW.T, String] :: HNil, (SegmentInput :: GetCall :: HNil, fooW.T :: HNil, String :: HNil, Foo)]
  testCompile[GetElement[Foo] :: QueryParam[fooW.T, String] :: HNil, (QueryInput :: GetCall :: HNil, fooW.T :: HNil, String :: HNil, Foo)]
  testCompile[GetElement[Foo] :: QueryParam[fooW.T, List[String]] :: HNil, (QueryInput :: GetCall :: HNil, fooW.T :: HNil, List[String] :: HNil, Foo)]
  testCompile[GetElement[Foo] :: HeaderParam[fooW.T, String] :: HNil, (HeaderInput :: GetCall :: HNil, fooW.T :: HNil, String :: HNil, Foo)]
  testCompile[GetElement[Foo] :: RawHeadersParam.type :: HNil, (RawHeadersInput :: GetCall :: HNil, RawHeadersField.T :: HNil, Map[String, String] :: HNil, Foo)]

  testCompile[
    GetElement[Foo] :: HeaderParam[fooW.T, Boolean] :: QueryParam[fooW.T, Int] :: SegmentParam[fooW.T, String] :: PathElement[pathW.T] :: HNil, 
    (pathW.T :: SegmentInput :: QueryInput :: HeaderInput :: GetCall :: HNil, fooW.T :: fooW.T :: fooW.T :: HNil, String :: Int :: Boolean :: HNil, Foo)
  ]
}
