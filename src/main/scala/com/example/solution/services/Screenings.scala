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

  def listScreenings(interval: ScreeningInterval): F[List[ScreeningData]]

  def pickScreening(screeningId: Int): F[Option[RoomData]]

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

  override def listScreenings(interval: ScreeningInterval): F[List[ScreeningData]] =
    sessionPool.use { session => {
      session.prepare(selectAllInRange).use { ps =>
        ps.stream((interval.start, interval.finish), chunkSize = 1024).compile.toList
      }.map(listScreenings => listScreenings.sorted)
    }
    }


  override def pickScreening(screeningId: Int): F[Option[RoomData]] = sessionPool.use { session =>
    session.prepare(selectRoomInfo).use { ps =>
      ps.stream(screeningId, 1024).compile.toList
        .map(convertToRoomData)
    }
  }


  private def convertToRoomData(list: List[Either[SimpleRoomData, FullRoomData]]): Option[RoomData] = {
    if (list.isEmpty)
      return None
    if (list.exists(_.isRight))
      Some(handleFullRoomData(list))
    else
      list.collectFirst {
        case Left(value) => RoomData(
          Room(RoomId(value.roomId), value.roomRows, value.roomEatsPerRow),
          for {
            row <- (1 to value.roomRows).toList
            seatInRow <- (1 to value.roomEatsPerRow).toList
          } yield AvailableSeat(row, seatInRow),
          value.screeningTime
        )
      }


  }

  private def handleFullRoomData(list: List[Either[SimpleRoomData, FullRoomData]]): RoomData = {
    val takenSeats = list.collect {
      case Right(fullRoomData) => (fullRoomData.takenRow, fullRoomData.takenSeatRow)
    }
    list.collectFirst {
      case Right(value) => RoomData(
        Room(RoomId(value.roomId), value.roomRows, value.roomSeatsPerRow),
        (for {
          row <- (1 to value.roomRows).toList
          seatInRow <- (1 to value.roomSeatsPerRow).toList
        } yield AvailableSeat(row, seatInRow))
          .filter(as => !takenSeats.contains(as.row, as.seatInRow)),
        value.screeningTime
      )
    }.get
    //ugly get
  }

}

private object ScreeningQueries {


  val screeningDataCodec: Decoder[ScreeningData] =
    (int4 ~ varchar(50) ~ timestamp).map {
      case id ~ title ~ dateTime =>
        ScreeningData(id, title, dateTime)
    }


  case class SimpleRoomData(roomId: Int, roomRows: Int, roomEatsPerRow: Int, screeningTime : LocalDateTime)

  case class FullRoomData(
                           roomId: Int,
                           roomRows: Int,
                           roomSeatsPerRow: Int,
                           takenRow: Int,
                           takenSeatRow: Int,
                           screeningTime : LocalDateTime
                         )

  val roomDataDecoder: Decoder[Either[SimpleRoomData, FullRoomData]] =
    (int4 ~ int4 ~ int4 ~ int4.opt ~ int4.opt ~ timestamp).map {

      case roomId ~ roomRows ~ roomSeatsPerRow ~ takenRowOpt ~ takenSeatInRowOpt ~ screeningTime =>
        takenRowOpt.map2(takenSeatInRowOpt)((takenRow, takenSeatInRow) =>
          FullRoomData(roomId,
            roomRows,
            roomSeatsPerRow,
            takenRow,
            takenSeatInRow,
            screeningTime))
          .toRight(SimpleRoomData(roomId, roomRows, roomSeatsPerRow,screeningTime))

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

  val selectRoomInfo: Query[Int, Either[SimpleRoomData, FullRoomData]] =
    sql"""
       select r.id, r.rows, r.seats_per_row,
            ts.row, ts.seat_in_row,s.screening_time from rooms r
            inner join screenings s
            on s.room_id = r.id
            left join reservations res
            on s.id = res.screening_id
            left join taken_seats ts
            on res.id = ts.reservation_id
            where s.id = $int4
       """.query(roomDataDecoder)
}
