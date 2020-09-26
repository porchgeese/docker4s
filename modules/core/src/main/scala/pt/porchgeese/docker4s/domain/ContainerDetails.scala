package pt.porchgeese.docker4s.domain

import com.github.dockerjava.api.command.InspectContainerResponse
import cats.implicits._
import pt.porchgeese.docker4s.domain.ContainerHealth.ContainerHealth
import pt.porchgeese.docker4s.domain.ContainerStatus.ContainerStatus

import scala.jdk.CollectionConverters._
import scala.util.Try

final case class ContainerDetails(
    image: ImageId,
    containerId: ContainerId,
    exposedPort: Map[ExposedPort, List[ContainerPort]],
    runningStatus: ContainerStatus,
    health: Option[ContainerHealth],
    name: ContainerName
)

object ContainerDetails {
  def fromInternalDetails(details: InspectContainerResponse): Either[Throwable, ContainerDetails] =
    Try {
      val imageId = ImageId(details.getImageId)
      val portBindings = details.getNetworkSettings.getPorts.getBindings.asScala.toList
        .map {
          case (port, bindigs) =>
            val exposePort = ExposedPort.fromInternalExposedPort(port)
            val bindings   = bindigs.toList.map(x => Try(ContainerPort(x.getHostPortSpec.toInt)).toEither).sequence
            bindings.map(b => exposePort -> b)
        }
        .sequence
        .map(_.toMap)
      val containerName = ContainerName(details.getName)
      val containerId   = ContainerId(details.getId)
      val state         = ContainerStatus.fromInternal(details.getState.getStatus)
      val health        = Try(details.getState.getHealth.getStatus).toOption.map(ContainerHealth.fromInternal)
      portBindings.map(b => ContainerDetails(imageId, containerId, b, state, health, containerName))
    }.toEither.flatten
}
