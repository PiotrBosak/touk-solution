package com.example.solution.http.routes

import cats.Monad
import cats.Defer
import com.example.solution.dto.reservation.ReservationData
import com.example.solution.services.Reservations
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import com.example.solution.http.json._
import cats.syntax.all._
import org.http4s.circe.{JsonDecoder, toMessageSynax}
import org.http4s.server.Router

final class ReservationRoutes[F[_] : Defer : JsonDecoder : Monad](
                                                                   reservations: Reservations[F]
                                                                 ) extends Http4sDsl[F] {

  private[routes] val prefixPath = "/reservations"


  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case ar@POST -> Root / "make" =>
      ar.asJsonDecode[ReservationData].flatMap(rd =>
        reservations.makeReservation(rd).flatMap {
          case Some(reservation) => Ok(reservation)
          case None => BadRequest()
        })
  }


  val routes : HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )
}
