package com.example.solution.services

import com.example.solution.domain.screening.Screening
import com.example.solution.dto.screening.RoomData

import java.time.LocalDateTime

trait Screenings[F[_]] {

  def listScreenings(start : LocalDateTime, finish : LocalDateTime) : F[List[Screening]]

  def pickScreening(screeningId : Int) : F[RoomData]

}
