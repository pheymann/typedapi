package typedapi

import typedapi.shared._
import shapeless._

// compilation-only test
object ApiDefinitionSpec {

  case class Foo()

  val testW  = Witness("test")
  val test2W = Witness("test2")
  val fooW   = Witness('foo)
  val barW   = Witness('bah)

  type Base = PathElement[testW.T] :: HNil

  class TestHelper[Act <: HList] {

    def apply[Exp <: HList](implicit ev: Act =:= Exp) = Unit
  }

  // path lists
  def testP[Act <: HList](actual: PathList[Act]) = new TestHelper[Act]
  testP(Path)[HNil]
  testP(Path :> "test")[PathElement[testW.T] :: HNil]
  testP(Path :> "test" :> "test2")[PathElement[test2W.T] :: PathElement[testW.T] :: HNil]
  testP(Path :> "test" :> Segment[Int]('foo))[SegmentParam[fooW.T, Int] :: PathElement[testW.T] :: HNil]
  testP(Path :> Segment[Int]('foo) :> "test")[PathElement[testW.T] :: SegmentParam[fooW.T, Int] :: HNil]

  // query lists
  def testQ[Act <: HList](actual: QueryList[Act]) = new TestHelper[Act]
  testQ(Queries)[HNil]
  testQ(Queries :> Query[Int](fooW))[QueryParam[fooW.T, Int] :: HNil]
  testQ(Queries :> Query[Int](fooW) :> Query[Int](barW))[QueryParam[barW.T, Int] :: QueryParam[fooW.T, Int] :: HNil]

  // header lists
  def testH[Act <: HList](actual: HeaderList[Act]) = new TestHelper[Act]
  testH(Headers)[HNil]
  testH(Headers :> Header[Int](fooW))[HeaderParam[fooW.T, Int] :: HNil]
  testH(Headers :> Header[Int](fooW) :> Header[Int](barW))[HeaderParam[barW.T, Int] :: HeaderParam[fooW.T, Int] :: HNil]

  def test[Act <: HList](actual: ApiTypeCarrier[Act]) = new TestHelper[Act]
  // methods
  test(api(Get[Foo]))[GetElement[Foo] :: HNil]
  test(api(Put[Foo]))[PutElement[Foo] :: HNil]
  test(api(Post[Foo]))[PostElement[Foo] :: HNil]
  test(api(Delete[Foo]))[DeleteElement[Foo] :: HNil]

  // whole api
  test(
    api(Get[Foo], Path :> "test" :> Segment[Int]('foo), Queries :> Query[String]('foo), Headers :> Header[Double]('foo))
  )[GetElement[Foo] :: HeaderParam[fooW.T, Double] :: QueryParam[fooW.T, String] :: SegmentParam[fooW.T, Int] :: PathElement[testW.T] :: HNil]
}
