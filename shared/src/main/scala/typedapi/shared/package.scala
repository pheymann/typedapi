package typedapi

import shapeless.Witness

package object shared {

  final val BodyField       = Witness('body)
  final val RawHeadersField = Witness('rawHeaders)
}
