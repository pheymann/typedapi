package typedapi

import typedapi.shared._
import shapeless._

import SpecUtil._

// compilation-only test
object ApiDefinitionSpec {

  case class Foo()

  val testW  = Witness("test")
  val test2W = Witness("test2")
  val fooW   = Witness('foo)
  val barW   = Witness('bah)

  type Base = PathElement[testW.T] :: HNil

  // path lists
  testCompile(Root)[HNil]
  testCompile(Root / "test")[PathElement[testW.T] :: HNil]
  testCompile(Root / "test" / "test2")[PathElement[test2W.T] :: PathElement[testW.T] :: HNil]
  testCompile(Root / "test" / Segment[Int]('foo))[SegmentParam[fooW.T, Int] :: PathElement[testW.T] :: HNil]
  testCompile(Root / Segment[Int]('foo) / "test")[PathElement[testW.T] :: SegmentParam[fooW.T, Int] :: HNil]

  // query lists
  testCompile(Queries)[HNil]
  testCompile(Queries add[Int](fooW))[QueryParam[fooW.T, Int] :: HNil]
  testCompile(Queries add[Int](fooW) add[Int](barW))[QueryParam[barW.T, Int] :: QueryParam[fooW.T, Int] :: HNil]

  // header lists
  testCompile(Headers)[HNil]
  testCompile(Headers add[Int](fooW))[HeaderParam[fooW.T, Int] :: HNil]
  testCompile(Headers add[Int](fooW) add[Int](barW))[HeaderParam[barW.T, Int] :: HeaderParam[fooW.T, Int] :: HNil]
  testCompile(Headers add(fooW, testW))[FixedHeaderElement[fooW.T, testW.T] :: HNil]
  testCompile(Headers client(fooW, testW))[ClientHeaderElement[fooW.T, testW.T] :: HNil]
  testCompile(Headers client[String](fooW))[ClientHeaderParam[fooW.T, String] :: HNil]
  testCompile(Headers serverSend(fooW, testW))[ServerHeaderSendElement[fooW.T, testW.T] :: HNil]
  testCompile(Headers serverMatch[String](fooW))[ServerHeaderMatchParam[fooW.T, String] :: HNil]

  // methods
  testCompile(api(Get[Json, Foo]))[GetElement[`Application/json`, Foo] :: HNil]
  test.illTyped("apiWothBody(Get[Foo], ReqBody[Foo])")
  testCompile(api(Put[Json, Foo]))[PutElement[`Application/json`, Foo] :: HNil]
  testCompile(apiWithBody(Put[Json, Foo], ReqBody[Plain, Foo]))[PutWithBodyElement[`Text/plain`, Foo, `Application/json`, Foo] :: HNil]
  testCompile(api(Post[Json, Foo]))[PostElement[`Application/json`, Foo] :: HNil]
  testCompile(apiWithBody(Post[Json, Foo], ReqBody[Plain, Foo]))[PostWithBodyElement[`Text/plain`, Foo, `Application/json`, Foo] :: HNil]
  testCompile(api(Delete[Json, Foo]))[DeleteElement[`Application/json`, Foo] :: HNil]
  test.illTyped("apiWothBody(Delete[Json, Foo], ReqBody[Plain, Foo])")

  // whole api
  testCompile(
    api(Get[Json, Foo], Root / "test" / Segment[Int]('foo), Queries add[String]('foo), Headers add[Double]('foo))
  )[GetElement[`Application/json`, Foo] :: HeaderParam[fooW.T, Double] :: QueryParam[fooW.T, String] :: SegmentParam[fooW.T, Int] :: PathElement[testW.T] :: HNil]
}
