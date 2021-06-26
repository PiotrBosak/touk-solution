package com.example.solution.dto

import com.example.solution.domain.room.Room

import java.time.LocalDateTime


object screening {
  case class AvailableSeat(row: Int, seatInRow: Int)

  case class RoomData(room : Room, availableSeats : List[AvailableSeat], screeningTime : LocalDateTime)

  case class ScreeningData(screeningId : Int, movieTitle : String, time : LocalDateTime)


  implicit object ScreeningDataOrdering extends Ordering[ScreeningData]{
    override def compare(s1: ScreeningData, s2: ScreeningData): Int =
      if (s1.time == s2.time) s1.movieTitle.compareTo(s2.movieTitle)
      else s1.time.compareTo(s2.time)
  }

}

