package typedapi

import typedapi.shared._
import shapeless.Witness

package object dsl {

  def := = EmptyCons

  def Path[S](wit: Witness.Lt[S]) = PathElement[S](wit)
  def Segment[V]                  = new PairTypeFromWitnessKey[SegmentParam, V]
  def Query[V]                    = new PairTypeFromWitnessKey[QueryParam, V]
  def Header[V]                   = new PairTypeFromWitnessKey[HeaderParam, V]

  type Json  = `Application/Json`.type
  type Plain = `Text/Plain`.type

  def ReqBody[MT <: MediaType, A] = ReqBodyElement[MT, A]
  def Get[MT <: MediaType, A] = GetElement[MT, A]
  def Put[MT <: MediaType, A] = PutElement[MT, A]
  def Post[MT <: MediaType, A] = PostElement[MT, A]
  def Delete[MT <: MediaType, A] = DeleteElement[MT, A]
}
