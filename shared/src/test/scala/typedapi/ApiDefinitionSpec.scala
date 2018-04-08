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
  testCompile(Root :> "test")[PathElement[testW.T] :: HNil]
  testCompile(Root :> "test" :> "test2")[PathElement[test2W.T] :: PathElement[testW.T] :: HNil]
  testCompile(Root :> "test" :> Segment[Int]('foo))[SegmentParam[fooW.T, Int] :: PathElement[testW.T] :: HNil]
  testCompile(Root :> Segment[Int]('foo) :> "test")[PathElement[testW.T] :: SegmentParam[fooW.T, Int] :: HNil]

  // query lists
  testCompile(Queries)[HNil]
  testCompile(Queries :> Query[Int](fooW))[QueryParam[fooW.T, Int] :: HNil]
  testCompile(Queries :> Query[Int](fooW) :> Query[Int](barW))[QueryParam[barW.T, Int] :: QueryParam[fooW.T, Int] :: HNil]

  // header lists
  testCompile(Headers)[HNil]
  testCompile(Headers :> Header[Int](fooW))[HeaderParam[fooW.T, Int] :: HNil]
  testCompile(Headers :> Header[Int](fooW) :> Header[Int](barW))[HeaderParam[barW.T, Int] :: HeaderParam[fooW.T, Int] :: HNil]

  // raw headers
  testCompile(Headers :> RawHeaders)[RawHeadersParam.type :: HNil]
  testCompile(Headers :> Header[Int](fooW) :> RawHeaders)[RawHeadersParam.type :: HeaderParam[fooW.T, Int] :: HNil]
  test.illTyped("Headers :> RawHeaders :> Header[Int](fooW)")

  // methods
  testCompile(api(Get[Foo]))[GetElement[Foo] :: HNil]
  test.illTyped("apiWothBody(Get[Foo], ReqBody[Foo])")
  testCompile(api(Put[Foo]))[PutElement[Foo] :: HNil]
  testCompile(apiWithBody(Put[Foo], ReqBody[Foo]))[PutWithBodyElement[Foo, Foo] :: HNil]
  testCompile(api(Post[Foo]))[PostElement[Foo] :: HNil]
  testCompile(apiWithBody(Post[Foo], ReqBody[Foo]))[PostWithBodyElement[Foo, Foo] :: HNil]
  testCompile(api(Delete[Foo]))[DeleteElement[Foo] :: HNil]
  test.illTyped("apiWothBody(Delete[Foo], ReqBody[Foo])")

  // whole api
  testCompile(
    api(Get[Foo], Root :> "test" :> Segment[Int]('foo), Queries :> Query[String]('foo), Headers :> Header[Double]('foo))
  )[GetElement[Foo] :: HeaderParam[fooW.T, Double] :: QueryParam[fooW.T, String] :: SegmentParam[fooW.T, Int] :: PathElement[testW.T] :: HNil]
}
