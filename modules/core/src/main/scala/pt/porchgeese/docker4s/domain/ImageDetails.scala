package pt.porchgeese.docker4s.domain

import com.github.dockerjava.api.command.InspectImageResponse

import scala.util.Try

final case class ImageDetails(
    image: ImageId,
    ports: List[ExposedPort]
)

object ImageDetails {
  def fromInternalDetails(details: InspectImageResponse): Either[Throwable, ImageDetails] =
    Try {
      val imageId      = ImageId(details.getId)
      val portBindings = details.getConfig.getExposedPorts.toList.map(ExposedPort.fromInternalExposedPort)
      ImageDetails(imageId, portBindings)
    }.toEither
}
