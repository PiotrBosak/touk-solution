package com.example.solution.dto

import com.example.solution.dto.screening.ScreeningData

import java.time.LocalDateTime

object reservation {

  case class PickedSeat(row : Int, seatInRow : Int, ticketType : String)

  case class ReservationData(
                            screeningId : Int,
                            pickedSeats: List[PickedSeat],
                            name : String,
                            surname : String
                            )

  case class ScreeningInterval(start : LocalDateTime, finish : LocalDateTime)



  case class MadeReservation(total : Double, expirationTime : LocalDateTime)

}
