package typedapi.shared

import shapeless._

// compilation-only test
final class ApiTransformerSpec extends TypeLevelFoldLeftLowPrio with ApiTransformer {

  case class Foo()

  def testCompile[H <: HList, Out](implicit folder: TypeLevelFoldLeft.Aux[H, Unit, Out]): TypeLevelFoldLeft.Aux[H, Unit, Out] = 
    folder

  val pathW = Witness("test")
  val fooW  = Witness('foo)

  testCompile[GetElement[Foo] :: HNil, (HNil, HNil, HNil, GetCall, Foo)]
  testCompile[PutElement[Foo] :: HNil, (HNil, HNil, HNil, PutCall, Foo)]
  testCompile[PutWithBodyElement[Foo, Foo] :: HNil, (HNil, BodyField.T :: HNil, Foo :: HNil, PutWithBodyCall, Foo)]
  testCompile[PostElement[Foo] :: HNil, (HNil, HNil, HNil, PostCall, Foo)]
  testCompile[PostWithBodyElement[Foo, Foo] :: HNil, (HNil, BodyField.T :: HNil, Foo :: HNil, PostWithBodyCall, Foo)]
  testCompile[DeleteElement[Foo] :: HNil, (HNil, HNil, HNil, DeleteCall, Foo)]
  testCompile[GetElement[Foo] :: PathElement[pathW.T] :: HNil, (pathW.T :: HNil, HNil, HNil, GetCall, Foo)]
  testCompile[GetElement[Foo] :: SegmentParam[fooW.T, String] :: HNil, (SegmentInput :: HNil, fooW.T :: HNil, String :: HNil, GetCall, Foo)]
  testCompile[GetElement[Foo] :: QueryParam[fooW.T, String] :: HNil, (QueryInput :: HNil, fooW.T :: HNil, String :: HNil, GetCall, Foo)]
  testCompile[GetElement[Foo] :: QueryParam[fooW.T, List[String]] :: HNil, (QueryInput :: HNil, fooW.T :: HNil, List[String] :: HNil, GetCall, Foo)]
  testCompile[GetElement[Foo] :: HeaderParam[fooW.T, String] :: HNil, (HeaderInput :: HNil, fooW.T :: HNil, String :: HNil, GetCall, Foo)]
  testCompile[GetElement[Foo] :: RawHeadersParam.type :: HNil, (RawHeadersInput :: HNil, RawHeadersField.T :: HNil, Map[String, String] :: HNil, GetCall, Foo)]

  testCompile[
    GetElement[Foo] :: HeaderParam[fooW.T, Boolean] :: QueryParam[fooW.T, Int] :: SegmentParam[fooW.T, String] :: PathElement[pathW.T] :: HNil, 
    (pathW.T :: SegmentInput :: QueryInput :: HeaderInput :: HNil, fooW.T :: fooW.T :: fooW.T :: HNil, String :: Int :: Boolean :: HNil, GetCall, Foo)
  ]
}
