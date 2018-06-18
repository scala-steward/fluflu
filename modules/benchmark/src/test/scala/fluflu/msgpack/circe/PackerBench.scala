package fluflu.msgpack.circe

import fluflu.msgpack.{States, models}
import io.circe.{Encoder, Json}
import io.circe.generic.auto._
import org.msgpack.core.MessageBufferPacker
import org.openjdk.jmh.annotations._

trait PackerBench {

  @inline private def encode[A: Encoder](a: A, p: MessageBufferPacker): Array[Byte] = {
    MessagePacker(p).encode(a)
    val r = p.toByteArray
    p.clear()
    r
  }

  @Benchmark
  def encodeUInt32Circe(data: States.PackData): Array[Byte] = {
    encode(data.UInt32, data.packer)
  }

  @Benchmark
  def encodeUInt64Circe(data: States.PackData): Array[Byte] = {
    encode(data.UInt64, data.packer)
  }

  @Benchmark
  def encodeStr16Circe(data: States.PackData): Array[Byte] = {
    encode(data.Str16, data.packer)
  }

  @Benchmark
  def encodeStr32Circe(data: States.PackData): Array[Byte] = {
    encode(data.Str32, data.packer)
  }

  @Benchmark
  def encodeLong10Circe(data: States.PackData): Array[Byte] = {
    encode(data.Long10CC, data.packer)
  }

  @Benchmark
  def encodeLong30Circe(data: States.PackData): Array[Byte] = {
    encode(data.Long30CC, data.packer)
  }

  @Benchmark
  def encodeLong60Circe(data: States.PackData): Array[Byte] = {
    encode(data.Long60CC, data.packer)
  }
}

trait PackAstBench {
  import io.circe.syntax._

  @inline private def encode[A: Encoder](a: A): Json = {
    a.asJson
  }

  @Benchmark
  def encodeLong10Circe(data: States.PackData): Json = {
    encode(data.Long10CC)
  }

  @Benchmark
  def encodeLong30Circe(data: States.PackData): Json = {
    encode(data.Long30CC)
  }

  @Benchmark
  def encodeLong60Circe(data: States.PackData): Json = {
    encode(data.Long60CC)
  }

  @Benchmark
  def encodeLong10CirceCache(data: States.PackData): Json = {
    encode(data.Long10CC)(Encoders.Long10CC)
  }

  @Benchmark
  def encodeLong30CirceCache(data: States.PackData): Json = {
    encode(data.Long30CC)(Encoders.Long30CC)
  }

  @Benchmark
  def encodeLong60CirceCache(data: States.PackData): Json = {
    encode(data.Long60CC)(Encoders.Long60CC)
  }
}

object Encoders {
  final val Long10CC: Encoder[models.Long10] = Encoder[models.Long10]
  final val Long30CC: Encoder[models.Long30] = Encoder[models.Long30]
  final val Long60CC: Encoder[models.Long60] = Encoder[models.Long60]
}
