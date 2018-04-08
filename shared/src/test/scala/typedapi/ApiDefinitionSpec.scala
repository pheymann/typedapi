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
  testCompile(Path)[HNil]
  testCompile(Path :> "test")[PathElement[testW.T] :: HNil]
  testCompile(Path :> "test" :> "test2")[PathElement[test2W.T] :: PathElement[testW.T] :: HNil]
  testCompile(Path :> "test" :> Segment[Int]('foo))[SegmentParam[fooW.T, Int] :: PathElement[testW.T] :: HNil]
  testCompile(Path :> Segment[Int]('foo) :> "test")[PathElement[testW.T] :: SegmentParam[fooW.T, Int] :: HNil]

  // query lists
  testCompile(Queries)[HNil]
  testCompile(Queries :> Query[Int](fooW))[QueryParam[fooW.T, Int] :: HNil]
  testCompile(Queries :> Query[Int](fooW) :> Query[Int](barW))[QueryParam[barW.T, Int] :: QueryParam[fooW.T, Int] :: HNil]

  // header lists
  testCompile(Headers)[HNil]
  testCompile(Headers :> Header[Int](fooW))[HeaderParam[fooW.T, Int] :: HNil]
  testCompile(Headers :> Header[Int](fooW) :> Header[Int](barW))[HeaderParam[barW.T, Int] :: HeaderParam[fooW.T, Int] :: HNil]

  // methods
  testCompile(api(Get[Foo]))[GetElement[Foo] :: HNil]
  testCompile(api(Put[Foo]))[PutElement[Foo] :: HNil]
  testCompile(api(Post[Foo]))[PostElement[Foo] :: HNil]
  testCompile(api(Delete[Foo]))[DeleteElement[Foo] :: HNil]

  // whole api
  testCompile(
    api(Get[Foo], Path :> "test" :> Segment[Int]('foo), Queries :> Query[String]('foo), Headers :> Header[Double]('foo))
  )[GetElement[Foo] :: HeaderParam[fooW.T, Double] :: QueryParam[fooW.T, String] :: SegmentParam[fooW.T, Int] :: PathElement[testW.T] :: HNil]
}
