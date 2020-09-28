package pt.porchgeese.docker4s

import cats.{Applicative, Monad}
import cats.arrow.FunctionK
import cats.effect.IO.Pure
import cats.effect.{ConcurrentEffect, Resource}
import pt.porchgeese.docker4s.algebra.DockerAction
import pt.porchgeese.docker4s.domain.{ContainerDef, ContainerDetails, ContainerId, ImageDetails, ImageId, ImageName}

object implicits {

  sealed trait ExtensionInterface[Res[_], Eff[_]] {
    def pullImage(image: ImageName): Eff[Unit]
    def removeContainer(id: ContainerId): Eff[Unit]
    def pushImage(image: ImageName): Eff[Unit]
    def buildImage(dockerFile: String, imageName: ImageName): Eff[Unit]
    def buildContainer(c: ContainerDef): Res[ContainerId]
    def startContainer(container: ContainerId): Res[Unit]
    def killContainer(container: ContainerId): Eff[Unit]
    def getContainerDetails(container: ContainerId): Eff[Option[ContainerDetails]]
    def getImageDetails(image: ImageId): Eff[Option[ImageDetails]]
    def getImageDetailsByNameAndTag(image: ImageName): Eff[Option[ImageId]]
    def removeContainerIfExists(c: ContainerId): Eff[Unit]
    def killContainerIfRunning(c: ContainerId): Eff[Unit]
  }

  implicit class Docker4SRunExt[F[_]: Monad, A](cli: Docker4SClient.Docker4SClient[F]) extends ExtensionInterface[F, F] {
    def pullImage(image: ImageName): F[Unit] =
      DockerAction.pullImage[F](image).foldMap(cli)
    def removeContainer(id: ContainerId): F[Unit] =
      DockerAction.removeContainer[F](id).foldMap(cli)
    def pushImage(image: ImageName): F[Unit] =
      DockerAction.pushImage[F](image).foldMap(cli)
    def buildImage(dockerFile: String, imageName: ImageName): F[Unit] =
      DockerAction.buildImage[F](dockerFile, imageName).foldMap(cli)
    def buildContainer(c: ContainerDef): F[ContainerId] =
      DockerAction.buildContainer[F](c).foldMap(cli)
    def startContainer(container: ContainerId): F[Unit] =
      DockerAction.startContainer[F](container).foldMap(cli)
    def killContainer(container: ContainerId): F[Unit] =
      DockerAction.killContainer[F](container).foldMap(cli)
    def getContainerDetails(container: ContainerId): F[Option[ContainerDetails]] =
      DockerAction.getContainerDetails[F](container).foldMap(cli)
    def getImageDetails(image: ImageId): F[Option[ImageDetails]] =
      DockerAction.getImageDetails[F](image).foldMap(cli)
    def getImageDetailsByNameAndTag(image: ImageName): F[Option[ImageId]] =
      DockerAction.getImageDetailsByNameAndTag[F](image).foldMap(cli)
    def removeContainerIfExists(c: ContainerId): F[Unit] =
      DockerAction.removeContainerIfExists[F](c).foldMap(cli)
    def killContainerIfRunning(c: ContainerId): F[Unit] =
      DockerAction.killContainerIfRunning[F](c).foldMap(cli)
  }

  implicit class Docker4SResourceExt[F[_]: ConcurrentEffect, A](cli: FunctionK[DockerAction, Resource[F, *]]) extends ExtensionInterface[Resource[F, *], F] {
    def pullImage(image: ImageName): F[Unit] =
      DockerAction.pullImage[Resource[F, *]](image).foldMap(cli).use(Applicative[F].pure)
    def removeContainer(id: ContainerId): F[Unit] =
      DockerAction.removeContainer[Resource[F, *]](id).foldMap(cli).use(Applicative[F].pure)
    def pushImage(image: ImageName): F[Unit] =
      DockerAction.pushImage[Resource[F, *]](image).foldMap(cli).use(Applicative[F].pure)
    def buildImage(dockerFile: String, imageName: ImageName): F[Unit] =
      DockerAction.buildImage[Resource[F, *]](dockerFile, imageName).foldMap(cli).use(Applicative[F].pure)
    def buildContainer(c: ContainerDef): Resource[F, ContainerId] =
      DockerAction.buildContainer[Resource[F, *]](c).foldMap(cli)
    def startContainer(container: ContainerId): Resource[F, Unit] =
      DockerAction.startContainer[Resource[F, *]](container).foldMap(cli)
    def killContainer(container: ContainerId): F[Unit] =
      DockerAction.killContainer[Resource[F, *]](container).foldMap(cli).use(Applicative[F].pure)
    def getContainerDetails(container: ContainerId): F[Option[ContainerDetails]] =
      DockerAction.getContainerDetails[Resource[F, *]](container).foldMap(cli).use(Applicative[F].pure)
    def getImageDetails(image: ImageId): F[Option[ImageDetails]] =
      DockerAction.getImageDetails[Resource[F, *]](image).foldMap(cli).use(Applicative[F].pure)
    def getImageDetailsByNameAndTag(image: ImageName): F[Option[ImageId]] =
      DockerAction.getImageDetailsByNameAndTag[Resource[F, *]](image).foldMap(cli).use(Applicative[F].pure)
    def removeContainerIfExists(c: ContainerId): F[Unit] =
      DockerAction.removeContainerIfExists[Resource[F, *]](c).foldMap(cli).use(Applicative[F].pure)
    def killContainerIfRunning(c: ContainerId): F[Unit] =
      DockerAction.killContainerIfRunning[Resource[F, *]](c).foldMap(cli).use(Applicative[F].pure)
  }
}
