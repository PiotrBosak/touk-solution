package com.example.solution.domain

abstract sealed class Ticket(val price: Double) {
  override def toString: String = this match {
    case ChildTicket => "child"
    case AdultTicket => "adult"
    case StudentTicket => "student"
  }
}

object Ticket {
  def fromString(s : String) : Option[Ticket]  = s match {
    case "child" => Some(ChildTicket)
    case "adult" => Some(AdultTicket)
    case "student" => Some(StudentTicket)
    case _ => None
  }


}
case object ChildTicket extends Ticket(12.5)

case object AdultTicket extends Ticket(25)

case object StudentTicket extends Ticket(18)


