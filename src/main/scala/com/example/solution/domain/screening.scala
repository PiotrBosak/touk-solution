package com.example.solution.domain

import com.example.solution.domain.movie.Movie
import com.example.solution.domain.room.Room

import java.time.LocalDateTime

object screening {

  case class ScreeningId(id : Int) extends AnyVal
  case class Screening(
                      id : ScreeningId,
                      movie : Movie,
                      room : Room,
                      screeningDate : LocalDateTime
                      )



}
