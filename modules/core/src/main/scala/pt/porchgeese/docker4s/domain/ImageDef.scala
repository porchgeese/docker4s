package pt.porchgeese.docker4s.domain

import cats.Show
import cats.implicits._
sealed trait ImageDef
final case class ImageName(repository: String, tag: String) extends ImageDef
final case class DockerFile(path: String)                   extends ImageDef

object ImageName {
  implicit val show: Show[ImageName] = Show[String].contramap(in => s"${in.repository}:${in.tag}")
}
