package typedapi.dsl

import typedapi.SpecUtil._
import typedapi.shared._
import shapeless._

// compilation-only test
object ApiDslSpec {

  case class Foo()

  val testW = Witness("test")
  val fooW  = Witness('foo)
  val base  = := :> "test"

  type Base = PathElement[testW.T] :: HNil

  val a = Query[Int].apply(fooW)

  // empty path
  testCompile(:= :> Segment[Int](fooW))[SegmentParam[fooW.T, Int] :: HNil]
  testCompile(:= :> Query[Int](fooW))[QueryParam[fooW.T, Int] :: HNil]
  testCompile(:= :> Header[Int](fooW))[HeaderParam[fooW.T, Int] :: HNil]
  testCompile(:= :> Get[Json, Foo])[GetElement[`Application/json`, Foo] :: HNil]

  // path: add every element
  testCompile(base :> Segment[Int](fooW))[SegmentParam[fooW.T, Int] :: Base]
  testCompile(base :> Query[Int](fooW))[QueryParam[fooW.T, Int] :: Base]
  testCompile(base :> Header[Int](fooW))[HeaderParam[fooW.T, Int] :: Base]
  testCompile(base :> Get[Json, Foo])[GetElement[`Application/json`, Foo] :: Base]

  // segment: add every element
  val _baseSeg = base :> Segment[Int](fooW)

  type _BaseSeg = SegmentParam[fooW.T, Int] :: Base

  testCompile(_baseSeg :> Segment[Int](fooW))[SegmentParam[fooW.T, Int] :: _BaseSeg]
  testCompile(_baseSeg :> Query[Int](fooW))[QueryParam[fooW.T, Int] :: _BaseSeg]
  testCompile(_baseSeg :> Header[Int](fooW))[HeaderParam[fooW.T, Int] :: _BaseSeg]
  testCompile(_baseSeg :> Get[Json, Foo])[GetElement[`Application/json`, Foo] :: _BaseSeg]
  
  // query: add queries, headers, body and final
  val _baseQ = base :> Query[Int](fooW)

  type _BaseQ = QueryParam[fooW.T, Int] :: Base

  test.illTyped("_baseQ :> \"fail\"")
  test.illTyped("_baseQ :> Segment[Int](fooW)")
  testCompile(_baseQ :> Query[Int](fooW))[QueryParam[fooW.T, Int] :: _BaseQ]
  testCompile(_baseQ :> Header[Int](fooW))[HeaderParam[fooW.T, Int] :: _BaseQ]
  testCompile(_baseQ :> Get[Json, Foo])[GetElement[`Application/json`, Foo] :: _BaseQ]
  
  // header: add header, final
  val _baseH = base :> Header[Int](fooW)

  type _BaseH = HeaderParam[fooW.T, Int] :: Base

  test.illTyped("_baseH :> \"fail\"")
  test.illTyped("_baseH :> Segment[Int](fooW)")
  test.illTyped("_baseH :> Query[Int](fooW)")
  testCompile(_baseH :> Header[Int](fooW))[HeaderParam[fooW.T, Int] :: _BaseH]
  testCompile(_baseH :> Header(fooW, testW) :> Header[Int](fooW))[HeaderParam[fooW.T, Int] :: FixedHeaderElement[fooW.T, testW.T] :: _BaseH]
  testCompile(_baseH :> Client.Header[String](fooW))[ClientHeaderParam[fooW.T, String] :: _BaseH]
  testCompile(_baseH :> Client.Header(fooW, testW))[ClientHeaderElement[fooW.T, testW.T] :: _BaseH]
  testCompile(_baseH :> Server.Send(fooW, testW))[ServerHeaderSendElement[fooW.T, testW.T] :: _BaseH]
  testCompile(_baseH :> Server.Match[String](fooW))[ServerHeaderMatchParam[fooW.T, String] :: _BaseH]
  testCompile(_baseH :> Get[Json, Foo])[GetElement[`Application/json`, Foo] :: _BaseH]

  // request body: add put or post
  val _baseRB = base :> ReqBody[Plain, Foo]

  type _BaseRB = Base

  test.illTyped("_baseRB :> Segment[Int](fooW)")
  test.illTyped("_baseRB :> Query[Int](fooW)")
  test.illTyped("_baseRB :> Header[Int](fooW)")
  test.illTyped("_baseRB :> Get[Json, Foo]")
  testCompile(_baseRB :> Put[Json, Foo])[PutWithBodyElement[`Text/plain`, Foo, `Application/json`, Foo] :: _BaseRB]
  testCompile(_baseRB :> Post[Json, Foo])[PostWithBodyElement[`Text/plain`, Foo, `Application/json`, Foo] :: _BaseRB]

  // method: nothing at all
  val _baseF = base :> Get[Json, Foo]

  test.illTyped("_baseF :> Segment[Int](fooW)")
  test.illTyped("_baseF :> Query[Int](fooW)")
  test.illTyped("_baseF :> Header[Int](fooW)")
  test.illTyped("_baseF :> Get[Json, Foo]")
}
