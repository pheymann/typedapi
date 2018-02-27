package typedapi.client.test

final case class ReqInput(method: String,
                          uri: List[String],
                          queries: Map[String, List[String]],
                          headers: Map[String, String])

final case class ReqInputWithBody[Bd](method: String, 
                                      uri: List[String], 
                                      queries: Map[String, List[String]], 
                                      headers: Map[String, String],
                                      body: Bd)
