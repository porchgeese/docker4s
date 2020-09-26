package pt.porchgeese.docker4s.domain

import cats.Show
import cats.implicits._
case class EnvVar(key: String, value: String)

object EnvVar {
  implicit val show: Show[EnvVar] = implicitly[Show[String]].contramap(v => s"${v.key}=${v.value}")
}
