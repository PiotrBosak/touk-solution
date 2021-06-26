package com.example.solution

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.implicits._

import com.example.solution.http.routes.{ReservationRoutes, ScreeningRoutes}
import com.example.solution.services.{LiveReservations, LiveScreenings}
import natchez.Trace.Implicits.noop
import cats.syntax.all._
import org.http4s.server.blaze.BlazeServerBuilder
import skunk.Session

import scala.concurrent.ExecutionContext.global

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val session =  Session.pooled[IO](
      host = "localhost",
      port = 5432,
      user = "postgres",
      database = "touksolution",
      max = 10,
      debug = true
    )

    val algebras = session.evalMap(res => {
      val b = LiveScreenings.make[IO](res)
      b.flatMap(screenings => {
        IO(screenings).product(LiveReservations.make(res, screenings))
      })
    })

    algebras.use { tuple =>
      val (screenings, reservations) = tuple
      val services = new ScreeningRoutes[IO](screenings).routes <+> new ReservationRoutes[IO](reservations).routes
      val httpApp = services.orNotFound
      BlazeServerBuilder[IO](global)
        .bindHttp(8080, "localhost")
        .withHttpApp(httpApp)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)
    }

  }
}
