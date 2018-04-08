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

  // empty path
  testCompile(:= :> Segment[Int](fooW))[SegmentParam[fooW.T, Int] :: HNil]
  testCompile(:= :> Query[Int](fooW))[QueryParam[fooW.T, Int] :: HNil]
  testCompile(:= :> Header[Int](fooW))[HeaderParam[fooW.T, Int] :: HNil]
  testCompile(:= :> Get[Foo])[GetElement[Foo] :: HNil]

  // path: add every element
  testCompile(base :> Segment[Int](fooW))[SegmentParam[fooW.T, Int] :: Base]
  testCompile(base :> Query[Int](fooW))[QueryParam[fooW.T, Int] :: Base]
  testCompile(base :> Header[Int](fooW))[HeaderParam[fooW.T, Int] :: Base]
  testCompile(base :> Get[Foo])[GetElement[Foo] :: Base]

  // segment: add every element
  val _baseSeg = base :> Segment[Int](fooW)

  type _BaseSeg = SegmentParam[fooW.T, Int] :: Base

  testCompile(_baseSeg :> Segment[Int](fooW))[SegmentParam[fooW.T, Int] :: _BaseSeg]
  testCompile(_baseSeg :> Query[Int](fooW))[QueryParam[fooW.T, Int] :: _BaseSeg]
  testCompile(_baseSeg :> Header[Int](fooW))[HeaderParam[fooW.T, Int] :: _BaseSeg]
  testCompile(_baseSeg :> Get[Foo])[GetElement[Foo] :: _BaseSeg]
  
  // query: add queries, headers, body and final
  val _baseQ = base :> Query[Int](fooW)

  type _BaseQ = QueryParam[fooW.T, Int] :: Base

  shapeless.test.illTyped("_baseQ :> \"fail\"")
  shapeless.test.illTyped("_baseQ :> Segment[Int](fooW)")
  testCompile(_baseQ :> Query[Int](fooW))[QueryParam[fooW.T, Int] :: _BaseQ]
  testCompile(_baseQ :> Header[Int](fooW))[HeaderParam[fooW.T, Int] :: _BaseQ]
  testCompile(_baseQ :> Get[Foo])[GetElement[Foo] :: _BaseQ]
  
  // header: add header, final
  val _baseH = base :> Header[Int](fooW)

  type _BaseH = HeaderParam[fooW.T, Int] :: Base

  shapeless.test.illTyped("_baseH :> \"fail\"")
  shapeless.test.illTyped("_baseH :> Segment[Int](fooW)")
  shapeless.test.illTyped("_baseH :> Query[Int](fooW)")
  testCompile(_baseH :> Header[Int](fooW))[HeaderParam[fooW.T, Int] :: _BaseH]
  testCompile(_baseH :> Get[Foo])[GetElement[Foo] :: _BaseH]

  // raw headers: add final
  val _baseRH = base :> RawHeaders

  type _BaseRH = RawHeaders.type :: Base

  shapeless.test.illTyped("_baseRH :> \"fail\"")
  shapeless.test.illTyped("_baseRH :> Segment[Int](fooW)")
  shapeless.test.illTyped("_baseRH :> Query[Int](fooW)")
  testCompile(_baseRH :> Get[Foo])[GetElement[Foo] :: _BaseRH]

  // request body: add put or post
  val _baseRB = base :> ReqBody[Foo]

  type _BaseRB = Base

  shapeless.test.illTyped("_baseRB :> Segment[Int](fooW)")
  shapeless.test.illTyped("_baseRB :> Query[Int](fooW)")
  shapeless.test.illTyped("_baseRB :> Header[Int](fooW)")
  shapeless.test.illTyped("_baseRB :> Get[Foo]")
  testCompile(_baseRB :> Put[Foo])[PutWithBodyElement[Foo, Foo] :: _BaseRB]
  testCompile(_baseRB :> Post[Foo])[PostWithBodyElement[Foo, Foo] :: _BaseRB]

  // method: nothing at all
  val _baseF = base :> Get[Foo]

  shapeless.test.illTyped("_baseF :> Segment[Int](fooW)")
  shapeless.test.illTyped("_baseF :> Query[Int](fooW)")
  shapeless.test.illTyped("_baseF :> Header[Int](fooW)")
  shapeless.test.illTyped("_baseF :> Get[Foo]")
}
