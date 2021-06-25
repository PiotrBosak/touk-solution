package com.example.solution.dto

import com.example.solution.domain.room.Room

import java.time.LocalDateTime


object screening {
  case class AvailableSeat(row: Int, seatInRow: Int)

  case class RoomData(room : Room, availableSeats : List[AvailableSeat])

  case class ScreeningData(screeningId : Int, movieTitle : String, time : LocalDateTime)

}

