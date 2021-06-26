package com.example.solution.services

import cats.data.{NonEmptyList, OptionT}
import cats.effect._
import cats.syntax.all._
import com.example.solution.domain.Ticket
import com.example.solution.domain.user.{User, UserName, UserSurname}
import com.example.solution.dto.reservation.{MadeReservation, PickedSeat, ReservationData}
import com.example.solution.dto.screening.{AvailableSeat, RoomData}
import skunk._
import skunk.codec.all._
import skunk.implicits._

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

trait Reservations[F[_]] {

  def makeReservation(data: ReservationData): F[Option[MadeReservation]]


}

object LiveReservations {

  def make[F[_] : Sync](
                         sessionPool: Resource[F, Session[F]],
                         screenings: Screenings[F]
                       ): F[Reservations[F]] =
    Sync[F].delay(
      new LiveReservations[F](sessionPool, screenings)
    )
}

final class LiveReservations[F[_] : BracketThrow : Sync] private(
                                                                  sessionPool: Resource[F, Session[F]],
                                                                  screenings: Screenings[F]
                                                                ) extends Reservations[F] {

  import ReservationQueries._

  private def areSeatsValid(picked: List[PickedSeat],
                            available: List[AvailableSeat],
                            seatsPerRow: Int): Boolean = {
    val isSeatAlreadyTaken = picked.exists(ps => !available.contains(AvailableSeat(ps.row, ps.seatInRow)))
    val leftSeats = available diff picked.map(ps => AvailableSeat(ps.row, ps.seatInRow))
    val leftOneEmptySeat = leftSeats.groupBy(_.row).exists(tuple => {
      val list = tuple._2
      list.exists(seat => {
        val seatInRow = seat.seatInRow
        (seatInRow > 1 && seatInRow < seatsPerRow) &&
          (!list.exists(as => as.seatInRow == seatInRow - 1) && !list.exists(as => as.seatInRow == seatInRow + 1))
      })
    })

    !(isSeatAlreadyTaken || leftOneEmptySeat)
  }


  private def getSeats(seats: List[PickedSeat]): OptionT[F, NonEmptyList[PickedSeat]] =
    OptionT(Sync[F].delay(seats.headOption match {
      case Some(value) => Option(NonEmptyList(value, seats.tail))
      case None => Option.empty[NonEmptyList[PickedSeat]]
    }))


  override def makeReservation(data: ReservationData): F[Option[MadeReservation]] = {

    val userWithSeats = (for {
      user <- OptionT(Sync[F].delay(User.mkUser(UserName(data.name), UserSurname(data.surname))))
      rd <- OptionT(screenings.pickScreening(data.screeningId))
      if rd.screeningTime.minus(15L, ChronoUnit.MINUTES).compareTo(LocalDateTime.now()) > 0

      pickedSeats <- getSeats(data.pickedSeats)
      seatsWithTickets <- OptionT(Sync[F].delay(pickedSeats.map(ps => for {
        seat <- Option(ps)
        ticket <- Ticket.fromString(ps.ticketType)
      } yield (seat, ticket)).sequence)) if areSeatsValid(seatsWithTickets.map(_._1).toList, rd.availableSeats, rd.room.seatsPerRow)

    } yield (user, seatsWithTickets)).value

    userWithSeats.flatMap {
      case None => Sync[F].delay(Option.empty[MadeReservation])
      case Some(tuple) =>
        val (user, seatsWithTickets) = (tuple._1, tuple._2.toList)
        sessionPool.use { session =>
          session.prepare(createReservation).use { cmd =>
            val totalCost = seatsWithTickets.map(_._2.price).sum
            cmd.execute((user, data.screeningId), totalCost).void
          } *> session.execute(selectMaxId).map(_.head).flatMap(id => {
            seatsWithTickets.traverse(tuple => {
              val (ps, ticket) = tuple
              session.prepare(createTakenSeat).use { cmd =>
                cmd.execute((id, ps), ticket).void
              }
            })
          }).map(_ => MadeReservation(
            seatsWithTickets.map(_._2.price).sum,
            LocalDateTime.now().plus(1L, ChronoUnit.HOURS))
          )
          //expiration date is obviously incorrect here
        }.map(mr => Option(mr))
    }
  }
}

private object ReservationQueries {


  val selectMaxId: Query[Void, Int] =
    sql"""
         SELECT MAX(id) FROM reservations
       """.query(int4)

  val createReservation: Command[User ~ Int ~ Double] =
    sql"""
         INSERT INTO reservations(client_name,client_surname,screening_id,total_cost)
         VALUES($varchar, $varchar, $int4, $numeric)
       """.command.contramap {
      case user ~ screeningId ~ cost =>
        user.name.value ~ user.surname.value ~ screeningId ~ cost
    }

  val createTakenSeat: Command[Int ~ PickedSeat ~ Ticket] =
    sql"""
         INSERT INTO taken_seats(reservation_id, row, seat_in_row, ticket_type)
         VALUES($int4, $int4, $int4, $varchar)
       """.command.contramap {
      case reservationId ~ pickedSeat ~ ticket =>
        reservationId ~ pickedSeat.row ~ pickedSeat.seatInRow ~ ticket.toString
    }

}

