
import akka.serialization.Serializer
import play.api.libs.json.{Format, Json, __}

class SFSReqSerializer extends Serializer {
  override def identifier: Int = 89675756

  override def toBinary(o: AnyRef): Array[Byte] = o match {
    case req: Message =>
      Json.toBytes(Json.toJson(req))
    case _ => Array.empty[Byte]
  }

  override def includeManifest: Boolean = false

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = Json.parse(bytes).as[Message]

  implicit val fmt: Format[Message] = Json.format[Message]
}

