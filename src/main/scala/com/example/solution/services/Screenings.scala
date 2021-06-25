package com.example.solution.services

import com.example.solution.domain.screening.Screening
import com.example.solution.dto.AvailableSeat

import java.time.LocalDateTime

trait Screenings[F[_]] {

  def listScreenings(start : LocalDateTime, finish : LocalDateTime) : List[Screening]

  def pickScreening(screeningId : Int) : List[AvailableSeat]

}
