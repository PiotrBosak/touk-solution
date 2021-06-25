package com.example.solution.services

import com.example.solution.dto.reservation.{MadeReservation, ReservationData}

trait Reservations[F[_]] {

  def makeReservation(data : ReservationData) : F[Option[MadeReservation]]

}
