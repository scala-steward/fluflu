package fluflu

import java.nio.ByteBuffer

import cats.syntax.either._
import io.circe.Json
import org.scalatest.WordSpec

import scala.util.{ Either => \/ }

class MessageUnpackerSpec extends WordSpec {
  import msgpack.MessageUnpacker

  val instance: Array[Byte] => MessageUnpacker = src => new MessageUnpacker(ByteBuffer.wrap(src))

  "nil" should {
    "be decoded" in {
      assert(instance(Array(0xc0.toByte)).decode[Json] === \/.right(Json.Null))
    }
  }

  "bool" should {
    "be decoded" in {
      assert(instance(Array(0xc2.toByte)).decode[Json] === \/.right(Json.fromBoolean(false)))
      assert(instance(Array(0xc3.toByte)).decode[Json] === \/.right(Json.fromBoolean(true)))
    }
  }

  "float32" should {
    "be decoded" in {
      val a = Array(0xca, 0x3f, 0x9d, 0x70, 0xa4).map(_.toByte)
      assert(instance(a).decode[Json] === \/.right(Json.fromDoubleOrNull(1.23f)))
    }
  }

  "float64" should {
    "be decoded" in {
      val a = Array(0xcb, 0x3f, 0xf3, 0xae, 0x14, 0x7a, 0xe1, 0x47, 0xae).map(_.toByte)
      assert(instance(a).decode[Json] === \/.right(Json.fromDoubleOrNull(1.23)))
    }
  }

  "int32" should {
    "be decoded" in {
      val a = Array(0xce, 0x7f, 0xff, 0xff, 0xff).map(_.toByte)
      assert(instance(a).decode[Json] === \/.right(Json.fromInt(Int.MaxValue)))
    }
  }

  "int64" should {
    "be decoded" in {
      val a = Array(0xcf, 0x7f, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff, 0xff).map(_.toByte)
      assert(instance(a).decode[Json] === \/.right(Json.fromLong(Long.MaxValue)))
    }
  }

  "fixstring" should {
    "be decoded" in {
      val a = Array(0xa3, 0x61, 0x62, 0x63).map(_.toByte) // "abc"
      assert(instance(a).decode[Json] === \/.right(Json.fromString("abc")))
    }
  }

  "string8" should {
    "be decoded" in {
      val a = Array(0xd9, 0x2d, 0x79, 0x75, 0x6c, 0x74, 0x73, 0x72, 0x69, 0x65, 0x6e, 0x69, 0x72, 0x65, 0x73, 0x74, 0x61, 0x6f, 0x69, 0x65, 0x6e, 0x73, 0x74, 0x74, 0x72, 0x69, 0x65, 0x6e, 0x69, 0x65, 0x6e, 0x69, 0x74, 0x73, 0x65, 0x72, 0x6e, 0x69, 0x74, 0x65, 0x6e, 0x69, 0x65, 0x6e, 0x74, 0x73, 0x72).map(_.toByte)
      val decoded = instance(a).decode[Json]
      assert(decoded === \/.right(Json.fromString("yultsrienirestaoiensttrienienitsernitenientsr")))
    }
  }

  "string16" should {
    "be decoded" in {
      val a = Array(0xda, 0x01, 0x00, 0x61, 0x61, 0x61, 0x73, 0x73, 0x73, 0x73, 0x73, 0x73, 0x73, 0x73, 0x73, 0x73, 0x73, 0x73, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x66, 0x66, 0x66, 0x66, 0x66, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x67, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x72, 0x5e, 0x2e, 0x2f, 0x60, 0x40, 0xe3, 0x80, 0x80, 0x66, 0x66, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x67, 0x67, 0x67, 0x67, 0x67, 0xe3, 0x81, 0x82, 0x73, 0x73, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0x73, 0x73, 0x73, 0x73, 0x73, 0x65, 0x26, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x65, 0x66, 0x66, 0x66, 0x66, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x7a, 0x38, 0x37, 0x31, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0xe2, 0x88, 0xab, 0x66, 0xe2, 0x84, 0xa2).map(_.toByte)
      val decoded = instance(a).decode[Json]
      assert(decoded === \/.right(Json.fromString("aaasssssssssssseeeeeeeeeeeeefffffzzzzzzzzggggggggggggggggggggggggrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr^./`@　ffeeeeeeeee000000000000fffffffffgggggあssffffffffffffffssssse&eeeeeeeeeeeeeeeeeeeeeeeeeeffffzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz8716666666666666∫f™")))
    }
  }

  "array" should {
    "decode flat array" in {
      val a = Array(0x93, 0xc3, 0xc2, 0x01).map(_.toByte) // [true, false, 1]
      val decoded = instance(a).decode[Json]
      assert(decoded === \/.right(Json.fromValues(Seq(Json.fromBoolean(true), Json.fromBoolean(false), Json.fromInt(1)))))
    }

    "decode nested array" in {
      val a = Array(0x94, 0xc3, 0x81, 0xa1, 0x61, 0xa3, 0x61, 0x62, 0x63, 0xc2, 0x93, 0x01, 0x02, 0x03).map(_.toByte)
      // [true, {"a":"abc"}, false, [1, 2, 3]]
      val decoded = instance(a).decode[Json]
      val json = io.circe.parser.parse("""[true, {"a":"abc"}, false, [1, 2, 3]]""")
      assert(decoded === json)
    }
  }

  "nested object" should {
    "be decoded" in {
      val nested = Array(0x85, 0xa3, 0x66, 0x6f, 0x6f, 0xa3, 0x62, 0x61, 0x72, 0xa4, 0x62, 0x75, 0x7a, 0x7a, 0x83, 0xa1, 0x78, 0xa1, 0x58, 0xa1, 0x79, 0xc3, 0xa1, 0x7a, 0xff, 0xa4, 0x68, 0x6f, 0x67, 0x65, 0xa4, 0x66, 0x75, 0x67, 0x61, 0xa1, 0x6e, 0xd3, 0xf0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xa1, 0x6d, 0xcb, 0x3f, 0xf1, 0x99, 0x99, 0x99, 0x99, 0x99, 0x9a).map(_.toByte)
      // {"foo": "bar", "buzz": { "x": "X", "y": true, "z": -1 }, "hoge": "fuga", "n": -1152921504606846976, "m": 1.1}
      val decoded = instance(nested).decode[Json]
      val nestedJson = io.circe.parser.parse("""{"foo": "bar", "buzz": { "x": "X", "y": true, "z": -1 }, "hoge": "fuga", "n": -1152921504606846976, "m": 1.1}""")
      assert(decoded === nestedJson)
    }
  }

  "map 16" should {
    "be decoded" in {
      val a = Array(0xde, 0x00, 0x10, 0xa1, 0x61, 0xcb, 0x3f, 0xf1, 0x99, 0x99, 0x99, 0x99, 0x99, 0x9a, 0xa1, 0x62, 0xcb, 0x40, 0x01, 0x99, 0x99, 0x99, 0x99, 0x99, 0x9a, 0xa1, 0x63, 0xcb, 0x40, 0x0a, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0xa1, 0x64, 0xcb, 0x40, 0x11, 0x99, 0x99, 0x99, 0x99, 0x99, 0x9a, 0xa1, 0x65, 0xcb, 0x40, 0x16, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xa1, 0x66, 0xcb, 0x40, 0x1a, 0x66, 0x66, 0x66, 0x66, 0x66, 0x66, 0xa1, 0x67, 0xcb, 0x40, 0x1e, 0xcc, 0xcc, 0xcc, 0xcc, 0xcc, 0xcd, 0xa1, 0x68, 0xcb, 0x40, 0x21, 0x99, 0x99, 0x99, 0x99, 0x99, 0x9a, 0xa1, 0x69, 0xcb, 0x40, 0x23, 0xcc, 0xcc, 0xcc, 0xcc, 0xcc, 0xcd, 0xa1, 0x6a, 0xcb, 0x40, 0x24, 0x33, 0x33, 0x33, 0x33, 0x33, 0x33, 0xa1, 0x6b, 0xcb, 0x40, 0x26, 0x38, 0x51, 0xeb, 0x85, 0x1e, 0xb8, 0xa1, 0x6c, 0xcb, 0x40, 0x28, 0x3d, 0x70, 0xa3, 0xd7, 0x0a, 0x3d, 0xa1, 0x6d, 0xcb, 0x40, 0x2a, 0x42, 0x8f, 0x5c, 0x28, 0xf5, 0xc3, 0xa1, 0x6e, 0xcb, 0x40, 0x2c, 0x47, 0xae, 0x14, 0x7a, 0xe1, 0x48, 0xa1, 0x6f, 0xcb, 0x40, 0x2e, 0x4c, 0xcc, 0xcc, 0xcc, 0xcc, 0xcd, 0xa1, 0x70, 0xcb, 0x40, 0x30, 0x28, 0xf5, 0xc2, 0x8f, 0x5c, 0x29).map(_.toByte)
      // {"a":1.1,"b":2.2,"c":3.3,"d":4.4,"e":5.5,"f":6.6,"g":7.7,"h":8.8,"i":9.9,"j":10.1,"k":11.11,"l":12.12,"m":13.13,"n":14.14,"o":15.15,"p":16.16}
      val Right(parsed) = io.circe.parser.parse("""{"a":1.1,"b":2.2,"c":3.3,"d":4.4,"e":5.5,"f":6.6,"g":7.7,"h":8.8,"i":9.9,"j":10.1,"k":11.11,"l":12.12,"m":13.13,"n":14.14,"o":15.15,"p":16.16}""")
      val Right(decoded) = instance(a).decode[Json]
      assert(parsed === decoded)
    }
  }

}
