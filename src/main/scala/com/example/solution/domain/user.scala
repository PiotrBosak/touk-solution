package com.example.solution.domain

object user {

  case class UserId(id : Int) extends AnyVal

  case class UserName(value : String) extends AnyVal

  case class UserSurname(value : String) extends AnyVal

  case class User(
                 id : UserId,
                 name : UserName,
                 surname : UserSurname
                 )

}
