package typedapi.client

import shapeless._
import org.specs2.mutable.Specification

final class ApiListSpec extends Specification {

  case class Foo()

  "ApiList is a helper construct to generate apis on the type level" >> {
    val testW = Witness("test")
    val fooW  = Witness('foo)
    val base  = := :> "test"

    type Base = Path[testW.T] :: HNil

    "empty path" >> {
      := :> Segment[Int](fooW) === SegmentCons[SegmentParam[fooW.T, Int] :: HNil]()
      := :> Query[Int](fooW) === QueryCons[QueryParam[fooW.T, Int] :: HNil]()
      := :> Header[Int](fooW) === HeaderCons[HeaderParam[fooW.T, Int] :: HNil]()
      := :> Get[Foo] === FinalCons[Get[Foo] :: HNil]()
    }

    "path: add every element" >> {
      base :> Segment[Int](fooW) === SegmentCons[SegmentParam[fooW.T, Int] :: Base]()
      base :> Query[Int](fooW) === QueryCons[QueryParam[fooW.T, Int] :: Base]()
      base :> Header[Int](fooW) === HeaderCons[HeaderParam[fooW.T, Int] :: Base]()
      base :> Get[Foo] === FinalCons[Get[Foo] :: Base]()
    }

    "segment: add every element" >> {
      val _base = base :> Segment[Int](fooW)

      type _Base = SegmentParam[fooW.T, Int] :: Base

      _base :> Segment[Int](fooW) === SegmentCons[SegmentParam[fooW.T, Int] :: _Base]()
      _base :> Query[Int](fooW) === QueryCons[QueryParam[fooW.T, Int] :: _Base]()
      _base :> Header[Int](fooW) === HeaderCons[HeaderParam[fooW.T, Int] :: _Base]()
      _base :> Get[Foo] === FinalCons[Get[Foo] :: _Base]()
    }

    "query: add queries, headers, body and final" >> {
      val _base = base :> Query[Int](fooW)

      type _Base = SegmentParam[fooW.T, Int] :: Base

      shapeless.test.illTyped("_base :> \"fail\"")
      shapeless.test.illTyped("_base :> Segment[Int](fooW)")
      _base :> Query[Int](fooW) === QueryCons[QueryParam[fooW.T, Int] :: _Base]()
      _base :> Header[Int](fooW) === HeaderCons[HeaderParam[fooW.T, Int] :: _Base]()
      _base :> Get[Foo] === FinalCons[Get[Foo] :: _Base]()
    }

    "header: add header, final" >> {
      val _base = base :> Header[Int](fooW)

      type _Base = HeaderParam[fooW.T, Int] :: Base

      shapeless.test.illTyped("_base :> \"fail\"")
      shapeless.test.illTyped("_base :> Segment[Int](fooW)")
      shapeless.test.illTyped("_base :> Query[Int](fooW)")
      _base :> Header[Int](fooW) === HeaderCons[HeaderParam[fooW.T, Int] :: _Base]()
      _base :> Get[Foo] === FinalCons[Get[Foo] :: _Base]()
    }

    "raw headers: add final" >> {
      val _base = base :> RawHeaders

      type _Base = RawHeaders.type :: Base

      shapeless.test.illTyped("_base :> \"fail\"")
      shapeless.test.illTyped("_base :> Segment[Int](fooW)")
      shapeless.test.illTyped("_base :> Query[Int](fooW)")
      _base :> Get[Foo] === FinalCons[Get[Foo] :: _Base]()
    }

    "request body: add put or post" >> {
      val _base = base :> ReqBody[Foo]

      type _Base = ReqBody[Foo] :: Base

      shapeless.test.illTyped("_base :> Segment[Int](fooW)")
      shapeless.test.illTyped("_base :> Query[Int](fooW)")
      shapeless.test.illTyped("_base :> Header[Int](fooW)")
      shapeless.test.illTyped("_base :> Get[Foo]")
      _base :> Put[Foo] === FinalCons[Put[Foo] :: _Base]()
      _base :> Post[Foo] === FinalCons[Post[Foo] :: _Base]()
    }

    "final: nothing at all" >> {
      val _base = base :> Get[Foo]

      shapeless.test.illTyped("_base :> Segment[Int](fooW)")
      shapeless.test.illTyped("_base :> Query[Int](fooW)")
      shapeless.test.illTyped("_base :> Header[Int](fooW)")
      shapeless.test.illTyped("_base :> Get[Foo]")

      _base === _base
    }
  }
}
