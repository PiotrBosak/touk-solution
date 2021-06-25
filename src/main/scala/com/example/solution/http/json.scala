package com.example.solution.http

import cats.Applicative
import com.example.solution.domain.movie.{Movie, MovieId, MovieTitle}
import com.example.solution.domain.room.{Room, RoomId}
import com.example.solution.domain.screening.{Screening, ScreeningId}
import com.example.solution.dto.reservation.ScreeningInterval
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.EntityEncoder
import org.http4s.circe.{jsonEncoderOf}

object json extends JsonCodecs {
  implicit def deriveEntityEncoder[F[_] : Applicative, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]
}


private[http] trait JsonCodecs {

  implicit val MovieIdDecoder: Decoder[MovieId] = deriveDecoder[MovieId]
  implicit val MovieIdEncoder: Encoder[MovieId] = deriveEncoder[MovieId]


  implicit val MovieTitleDecoder: Decoder[MovieTitle] = deriveDecoder[MovieTitle]
  implicit val MovieTitleEncoder: Encoder[MovieTitle] = deriveEncoder[MovieTitle]

  implicit val MovieDecoder: Decoder[Movie] = deriveDecoder[Movie]
  implicit val MovieEncoder: Encoder[Movie] = deriveEncoder[Movie]

  implicit val RoomIdDecoder: Decoder[RoomId] = deriveDecoder[RoomId]
  implicit val RoomIdEncoder: Encoder[RoomId] = deriveEncoder[RoomId]
  implicit val RoomDecoder: Decoder[Room] = deriveDecoder[Room]
  implicit val RoomEncoder: Encoder[Room] = deriveEncoder[Room]

  implicit val ScreeningIdDecoder: Decoder[ScreeningId] = deriveDecoder[ScreeningId]
  implicit val ScreeningIdEncoder: Encoder[ScreeningId] = deriveEncoder[ScreeningId]
  implicit val ScreeningDecoder: Decoder[Screening] = deriveDecoder[Screening]
  implicit val ScreeningEncoder: Encoder[Screening] = deriveEncoder[Screening]

  implicit val ScreeningIntervalDecoder : Decoder[ScreeningInterval] = deriveDecoder[ScreeningInterval]
  implicit val ScreeningIntervalEncoder : Encoder[ScreeningInterval] = deriveEncoder[ScreeningInterval]


}

