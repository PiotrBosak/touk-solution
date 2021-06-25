package com.example.solution.dto

import com.example.solution.domain.room.Room


object screening {
  case class AvailableSeat(row: Int, seatInRow: Int)

  case class RoomData(room : Room, availableSeats : List[AvailableSeat])

}

