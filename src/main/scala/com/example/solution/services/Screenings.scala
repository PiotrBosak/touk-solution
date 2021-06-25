package com.example.solution.services

import cats.effect._
import cats.syntax.all._
import com.example.solution.domain.room.{Room, RoomId}
import com.example.solution.domain.screening.Screening
import com.example.solution.dto.reservation.ScreeningInterval
import com.example.solution.dto.screening.{AvailableSeat, RoomData, ScreeningData}
import skunk._
import skunk.codec.all._
import skunk.implicits._

import java.time.LocalDateTime

trait Screenings[F[_]] {

  def listScreenings(interval : ScreeningInterval): F[List[ScreeningData]]

  def pickScreening(screeningId: Int): F[RoomData]

}

object LiveScreenings {
  def make[F[_] : Sync](
                         sessionPool: Resource[F, Session[F]]
                       ): F[Screenings[F]] =
    Sync[F].delay(
      new LiveScreenings[F](sessionPool)
    )
}

final class LiveScreenings[F[_] : BracketThrow : Sync] private(
                                                                sessionPool: Resource[F, Session[F]]
                                                              ) extends Screenings[F] {

  import ScreeningQueries._

  override def listScreenings(interval : ScreeningInterval): F[List[ScreeningData]] =
    sessionPool.use { session => {
      session.prepare(selectAllInRange).use { ps =>
        ps.stream((interval.start,interval.finish),chunkSize = 1024).compile.toList
      }
    }
    }


  override def pickScreening(screeningId: Int): F[RoomData] = sessionPool.use { session =>
    session.prepare(selectRoomInfo).use { ps =>
      ps.stream(screeningId, 1024).compile.toList
        .map(convertToRoomData)
    }
  }

  private def convertToRoomData(list: List[RoomDataRecord]): RoomData = {
    val roomId = list.head.roomId
    val roomRows = list.head.roomRows
    val roomSeatsPerRow = list.head.roomSeatsPerRow
    val takenSeats = list.map(rdr => (rdr.takenRow, rdr.takenSeatRow))
    val availableSeats = (for {
      row <- (1 to roomRows).toList
      seatInRow <- (1 to roomSeatsPerRow).toList
    } yield (row, seatInRow))
      .filter(t => !takenSeats.contains(t._1, t._2))
      .map(t => AvailableSeat(t._1, t._2))

    RoomData(
      Room(RoomId(roomId), roomRows, roomSeatsPerRow),
      availableSeats
    )
  }
}

private object ScreeningQueries {


  val screeningDataCodec: Decoder[ScreeningData] =
    (int4 ~ varchar(50) ~ timestamp).map {
      case id ~ title ~ dateTime =>
        ScreeningData(id, title, dateTime)
    }

  case class RoomDataRecord(
                             roomId: Int,
                             roomRows: Int,
                             roomSeatsPerRow: Int,
                             takenRow: Int,
                             takenSeatRow: Int
                           )

  val roomDataDecoder: Decoder[RoomDataRecord] =
    (int4 ~ int4 ~ int4 ~ int4 ~ int4).map {
      case roomId ~ roomRows ~ roomSeatsPerRow ~ takenRow ~ takenSeatInRow =>
        RoomDataRecord(roomId,
          roomRows,
          roomSeatsPerRow,
          takenRow,
          takenSeatInRow)

    }


  val selectAllInRange: Query[LocalDateTime ~ LocalDateTime, ScreeningData] =
    sql"""
         select s.id, m.title, s.screening_time
         from screenings s
         inner join movies m
         on s.movie_id = m.id
         where s.screening_time > ${timestamp}
         and s.screening_time < ${timestamp}
       """.query(screeningDataCodec)

  val selectRoomInfo: Query[Int, RoomDataRecord] =
    sql"""
       select r.id, r.rows, r.seats_per_row,
            ts.row, ts.seat_in_row from rooms r
            inner join screenings s
            on s.room_id = r.id
            inner join reservations res
            on res.screening_id = s.id
            inner join taken_seats ts
            on ts.reservation_id = res.id
            where s.id = $int4
       """.query(roomDataDecoder)
}
