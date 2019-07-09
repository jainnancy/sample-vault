package com.knoldus.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait VaultResponse

final case class Metadata(created_time: String, deletion_time: String, destroyed: Boolean, version: Int)

final case class Data(data: Map[String, String], metadata: Metadata)

final case class VaultSuccessResponse(request_id: String, lease_id: String, renewable: Boolean, lease_duration: Int,
                                      data: Data, wrap_info: Option[String], warnings: Option[String], auth: Option[String]) extends VaultResponse

final case class VaultFailureResponse(errors: List[String]) extends VaultResponse

object JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val metadataFormat: RootJsonFormat[Metadata] = jsonFormat4(Metadata)
  implicit val dataFormat: RootJsonFormat[Data] = jsonFormat2(Data)
  implicit val successResponseFormat: RootJsonFormat[VaultSuccessResponse] = jsonFormat8(VaultSuccessResponse)
  implicit val vaultFailureResponse = jsonFormat1(VaultFailureResponse)
}
