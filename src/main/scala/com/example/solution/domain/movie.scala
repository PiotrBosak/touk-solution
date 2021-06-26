package com.example.solution.domain

object movie {

  case class MovieId(id : Int) extends AnyVal
  case class MovieTitle(title : String) extends AnyVal

  case class Movie(id : MovieId, title: MovieTitle)

}
