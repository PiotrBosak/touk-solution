package com.example.solution.domain

object room {

  case class RoomId(id : Int) extends AnyVal
  case class Room(
                 id : RoomId,
                 rows : Int,
                 seatsPerRow : Int
                 )



}
