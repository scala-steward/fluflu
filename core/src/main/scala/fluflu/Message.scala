package fluflu

import java.nio.charset.StandardCharsets

import cats.syntax.either._
import io.circe.{ Encoder, Json, Printer }
import io.circe.syntax._

import scala.util.{ Either => \/ }

object Message {

  private[this] val packer = msgpack.MessagePacker()

  def pack[A](e: Event[A])(implicit A: Encoder[A]): Throwable \/ Array[Byte] = e match {
    case Event(prefix, label, record, time) =>
      packer pack (Json arr (
        Json fromString s"$prefix.$label",
        Json fromLong time,
        record.asJson
      ))
  }
}
