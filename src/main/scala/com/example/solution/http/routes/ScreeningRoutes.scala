package com.example.solution.http.routes

import cats.Monad
import cats.Defer
import com.example.solution.dto.reservation.ScreeningInterval
import com.example.solution.services.Screenings
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import com.example.solution.http.json._
import cats.syntax.validated
import cats.syntax.all._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import org.http4s.circe.{JsonDecoder, toMessageSynax}
import org.http4s.server.Router

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

final class ScreeningRoutes[F[_] : Defer : JsonDecoder : Monad](
                                                                 screenings: Screenings[F]
                                                               ) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/screenings"


  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {


    case ar@GET -> Root =>
      ar.asJsonDecode[ScreeningInterval].flatMap(s => {
        Ok(screenings.listScreenings(s))
      })

    case GET -> Root / "pick" / IntVar(screeningId) =>
      Ok(screenings.pickScreening(screeningId))

  }


  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
