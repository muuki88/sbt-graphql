package rocks.muki.example

import java.time.Instant

import io.circe._

/**
  * You can put custom circe codecs here so they will be picked up as long as you put this into the
  * `graphqlCodegenImports` sbt setting.
  */
object codecs {

  /**
    * This codec encodes `java.time.Instant` as unix epoch millis instead of iso8601 string (circe default).
    *
    * This has to match the server-side implementation for a custom "Instant" type.
    */
  implicit val instantCodec: Codec[Instant] = Codec.from(
    Decoder[Long].map(Instant.ofEpochMilli),
    Encoder[Long].contramap(_.toEpochMilli)
  )
}
