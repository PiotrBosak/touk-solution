package com.example.solution

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {

  val a = List(1,2,3)
  def run(args: List[String]) =
   IO(println("Hello")).as(ExitCode.Success)
}
