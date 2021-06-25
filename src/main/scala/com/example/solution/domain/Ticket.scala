package com.example.solution.domain

abstract sealed case class Ticket(price : Double)

case object ChildTicket extends Ticket(12.5)
case object AdultTicket extends Ticket(25)
case object StudentTicket extends Ticket(18)
