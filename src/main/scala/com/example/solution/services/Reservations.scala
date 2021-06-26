package com.example.solution.services

import cats.data.OptionT
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

  private def areSeatsValid(picked: List[PickedSeat], available: List[AvailableSeat]): Boolean = true

  override def makeReservation(data: ReservationData): F[Option[MadeReservation]] = {

    val userWithSeats =  (for {
      user <- OptionT(Sync[F].delay(User.mkUser(UserName(data.name), UserSurname(data.surname))))
      rd <- OptionT(screenings.pickScreening(data.screeningId))
      seatsWithTickets <- OptionT(Sync[F].delay(data.pickedSeats.map(ps => for {
        seat <- Option(ps)
        ticket <- Ticket.fromString(ps.ticketType)
      } yield (seat, ticket)).sequence)) if areSeatsValid(seatsWithTickets.map(_._1), rd.availableSeats)
    } yield (user, seatsWithTickets)).value

    userWithSeats.flatMap {
      case None => Sync[F].delay(Option.empty[MadeReservation])
      case Some(tuple) =>
        val (user, seatsWithTickets) = tuple
        sessionPool.use { session =>
          session.prepare(createReservation).use { cmd =>
            val totalCost = seatsWithTickets.map(_._2.price).sum
            cmd.execute((user, data.screeningId), totalCost).void
          } *> session.execute(selectMaxId).map(_.head).flatMap(id => {
            seatsWithTickets.traverse(t => {
              val (ps, ticket) = t
              session.prepare(createTakenSeat).use { cmd =>
                cmd.execute((id, ps), ticket).void
              }
            })
          }).map(_ => MadeReservation(seatsWithTickets.map(_._2.price).sum, LocalDateTime.now()))
        }.map(mr => Option(mr))
    }
  }
}

private object ReservationQueries {


  val selectMaxId: Query[Void, Int] =
    sql"""
         select max(id) from reservations
       """.query(int4)
  val createReservation: Command[User ~ Int ~ Double] =
    sql"""
         INSERT INTO reservations(client_name,client_surname,screening_id,total_cost)
         values($varchar, $varchar, $int4, $numeric)
       """.command.contramap {
      case user ~ screeningId ~ cost =>
        user.name.value ~ user.surname.value ~ screeningId ~ cost
    }

  val createTakenSeat: Command[Int ~ PickedSeat ~ Ticket] =
    sql"""
         insert into taken_seats(reservation_id, row, seat_in_row, ticket_type)
         values($int4, $int4, $int4, $varchar)
       """.command.contramap {
      case reservationId ~ pickedSeat ~ ticket =>
        reservationId ~ pickedSeat.row ~ pickedSeat.seatInRow ~ ticket.toString
    }

}

