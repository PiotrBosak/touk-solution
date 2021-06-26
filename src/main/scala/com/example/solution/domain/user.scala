package com.example.solution.domain

object user {


  case class UserName(value: String) extends AnyVal

  case class UserSurname(value: String) extends AnyVal

  sealed abstract case class User private (
                           name: UserName,
                           surname: UserSurname
                         )

  object User {
    def mkUser(username: UserName, userSurname: UserSurname): Option[User] = {
      val (name, surname) = (username.value, userSurname.value)
      if (name.length < 3 || surname.length < 3) None
      else if (!name.charAt(0).isUpper || !surname.charAt(0).isUpper) None
      else if (surname.contains('-')) {
        val secondPart = surname.split('-')(1)
        if (!secondPart.charAt(0).isUpper) None
        else Some(new User(username, userSurname){})
      }
      else
        Some(new User(username, userSurname){})
    }



  }

}
