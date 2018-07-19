package typedapi

import typedapi.shared._

package object dsl {

  def := = EmptyCons

  def Segment[V] = new PairTypeFromWitnessKey[SegmentParam, V]
  def Query[V]   = new PairTypeFromWitnessKey[QueryParam, V]
  def Header[V]  = new PairTypeFromWitnessKey[HeaderParam, V]
  def Fixed      = new PairTypeFromWitnesses[FixedHeaderElement]

  type Json  = `Application/Json`.type
  type Plain = `Text/Plain`.type

  def ReqBody[MT <: MediaType, A] = TypeCarrier[ReqBodyElement[MT, A]]()
  def Get[MT <: MediaType, A]     = TypeCarrier[GetElement[MT, A]]()
  def Put[MT <: MediaType, A]     = TypeCarrier[PutElement[MT, A]]()
  def Post[MT <: MediaType, A]    = TypeCarrier[PostElement[MT, A]]()
  def Delete[MT <: MediaType, A]  = TypeCarrier[DeleteElement[MT, A]]()
}
