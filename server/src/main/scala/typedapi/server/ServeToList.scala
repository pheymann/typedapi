package typedapi.server

import shapeless._

import scala.annotation.implicitNotFound

@implicitNotFound("Cannot find ServeToList instance to map Serve HList to List. Maybe you have different Request, Response types defined?.\n" + 
                  "  serves: ${H}\n  request: ${Req}\n  response: ${Resp}")
sealed trait ServeToList[H <: HList, Req, Resp] {

  def apply(h: H): List[Serve[Req, Resp]]
}

trait ServeToListLowPrio {

  implicit def hnilToList[Req, Resp] = new ServeToList[HNil, Req, Resp] {
    def apply(h: HNil): List[Serve[Req, Resp]] = Nil
  }

  implicit def serveToList[Req, Resp, T <: HList](implicit next: ServeToList[T, Req, Resp]) = new ServeToList[Serve[Req, Resp] :: T, Req, Resp] {
    def apply(h: Serve[Req, Resp] :: T): List[Serve[Req, Resp]] = h.head :: next(h.tail)
  }
}
