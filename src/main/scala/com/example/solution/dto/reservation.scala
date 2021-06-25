package com.example.solution.dto

import java.time.LocalDateTime

object reservation {

  case class PickedSeat(row : Int, seatInRow : Int, ticketType : String)

  case class ReservationData(
                            pickedSeats: List[PickedSeat],
                            name : String,
                            surname : String
                            )

  case class MadeReservation(total : Double, expirationTime : LocalDateTime)

}
