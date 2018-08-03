package typedapi

import typedapi.shared._

package object dsl extends MethodToReqBodyLowPrio with MethodToStringLowPrio with MediaTypes {

  def := = EmptyCons

  def Segment[V] = new PairTypeFromWitnessKey[SegmentParam, V]
  def Query[V]   = new PairTypeFromWitnessKey[QueryParam, V]
  def Header[V]  = new PairTypeFromWitnessKey[HeaderParam, V]
  def Header     = new PairTypeFromWitnesses[FixedHeaderElement]

  object Client {

    def Header    = new PairTypeFromWitnesses[ClientHeaderElement]
    def Header[V] = new PairTypeFromWitnessKey[ClientHeaderParam, V]
  }

  object Server {

    def Send      = new PairTypeFromWitnesses[ServerHeaderSendElement]
    def Match[V]  = new PairTypeFromWitnessKey[ServerHeaderMatchParam, V]
  }

  type Json  = `Application/json`
  type Plain = `Text/plain`

  def ReqBody[MT <: MediaType, A] = TypeCarrier[ReqBodyElement[MT, A]]()
  def Get[MT <: MediaType, A]     = TypeCarrier[GetElement[MT, A]]()
  def Put[MT <: MediaType, A]     = TypeCarrier[PutElement[MT, A]]()
  def Post[MT <: MediaType, A]    = TypeCarrier[PostElement[MT, A]]()
  def Delete[MT <: MediaType, A]  = TypeCarrier[DeleteElement[MT, A]]()
}
