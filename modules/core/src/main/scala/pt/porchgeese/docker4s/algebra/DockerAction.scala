package pt.porchgeese.docker4s.algebra

import cats.Applicative
import cats.free.FreeT
import pt.porchgeese.docker4s.domain.{ContainerDef, ContainerDetails, ContainerId, ContainerStatus, ImageDetails, ImageId, ImageName}
import pt.porchgeese.docker4s.label.Label

sealed trait DockerAction[A]

case class BuildImage(dockerFile: String, imageName: ImageName, labels: List[Label]) extends DockerAction[Unit]
case class BuildContainer(c: ContainerDef)                                           extends DockerAction[ContainerId]

case class RemoveContainer(c: ContainerId) extends DockerAction[Unit]

case class PullImage(image: ImageName) extends DockerAction[Unit]

case class PushImage(image: ImageName) extends DockerAction[Unit]

case class StartContainer(container: ContainerId) extends DockerAction[Unit]
case class KillContainer(container: ContainerId)  extends DockerAction[Unit]

case class GetContainerDetails(container: ContainerId)   extends DockerAction[Option[ContainerDetails]]
case class GetImageDetails(image: ImageId)               extends DockerAction[Option[ImageDetails]]
case class GetImageDetailsByRepoAndTag(image: ImageName) extends DockerAction[Option[ImageId]]

object DockerAction {
  type ActionOp[A, F[_]] = FreeT[DockerAction, F, A]
  def pullImage[F[_]: Applicative](image: ImageName): ActionOp[Unit, F]                                               = FreeT.liftF[DockerAction, F, Unit](PullImage(image))
  def removeContainer[F[_]: Applicative](id: ContainerId): ActionOp[Unit, F]                                          = FreeT.liftF[DockerAction, F, Unit](RemoveContainer(id))
  def pushImage[F[_]: Applicative](image: ImageName): ActionOp[Unit, F]                                               = FreeT.liftF[DockerAction, F, Unit](PushImage(image))
  def buildImage[F[_]: Applicative](dockerFile: String, imageName: ImageName, labels: List[Label]): ActionOp[Unit, F] = FreeT.liftF[DockerAction, F, Unit](BuildImage(dockerFile, imageName, labels))
  def buildContainer[F[_]: Applicative](c: ContainerDef): ActionOp[ContainerId, F]                                    = FreeT.liftF[DockerAction, F, ContainerId](BuildContainer(c))
  def startContainer[F[_]: Applicative](container: ContainerId): ActionOp[Unit, F]                                    = FreeT.liftF[DockerAction, F, Unit](StartContainer(container))
  def killContainer[F[_]: Applicative](container: ContainerId): ActionOp[Unit, F]                                     = FreeT.liftF[DockerAction, F, Unit](KillContainer(container))
  def getContainerDetails[F[_]: Applicative](container: ContainerId): ActionOp[Option[ContainerDetails], F]           = FreeT.liftF[DockerAction, F, Option[ContainerDetails]](GetContainerDetails(container))
  def getImageDetails[F[_]: Applicative](image: ImageId): ActionOp[Option[ImageDetails], F]                           = FreeT.liftF[DockerAction, F, Option[ImageDetails]](GetImageDetails(image))
  def getImageDetailsByNameAndTag[F[_]: Applicative](image: ImageName): ActionOp[Option[ImageId], F]                  = FreeT.liftF[DockerAction, F, Option[ImageId]](GetImageDetailsByRepoAndTag(image))
  def removeContainerIfExists[F[_]: Applicative](c: ContainerId): FreeT[DockerAction, F, Unit] =
    getContainerDetails(c).flatMap(_.fold(FreeT.pure[DockerAction, F, Unit](()))(_ => removeContainer(c)))
  def killContainerIfRunning[F[_]: Applicative](c: ContainerId): FreeT[DockerAction, F, Unit] =
    getContainerDetails(c).flatMap { details =>
      details
        .filter(cd => Set(ContainerStatus.RUNNING, ContainerStatus.PAUSED, ContainerStatus.REMOVING).contains(cd.runningStatus))
        .fold(FreeT.pure[DockerAction, F, Unit]())(det => killContainer(det.containerId))
    }
}
