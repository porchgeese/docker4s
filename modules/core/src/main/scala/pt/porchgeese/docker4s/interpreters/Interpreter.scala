package pt.porchgeese.docker4s.interpreters

import java.io.File

import cats.arrow.FunctionK
import cats.data.NonEmptyList
import cats.effect.concurrent.Deferred
import cats.effect.{Async, ConcurrentEffect, Effect, Resource, Timer}
import cats.~>
import com.github.dockerjava.api.{DockerClient => InternalDockerClient}
import com.github.dockerjava.api.command.{BuildImageResultCallback, CreateContainerCmd, PullImageResultCallback}
import com.github.dockerjava.api.model.{ExposedPort, HostConfig, PushResponseItem}
import pt.porchgeese.docker4s.algebra._
import pt.porchgeese.docker4s.domain._
import pt.porchgeese.docker4s.util.Util
import cats.implicits._
import cats.effect.implicits._
import com.github.dockerjava.api.async.ResultCallback
import com.github.dockerjava.api.exception.NotFoundException
import pt.porchgeese.docker4s.config.Docker4SConfig
import pt.porchgeese.docker4s.label.Label

import scala.jdk.CollectionConverters._
import scala.util.Try

object Interpreter {

  def effectInterpreter[F[_]: ConcurrentEffect: Timer](cli: InternalDockerClient, config: Docker4SConfig): FunctionK[DockerAction, F] =
    new (DockerAction ~> F) {
      override def apply[A](fa: DockerAction[A]): F[A] =
        fa match {
          case PullImage(image) =>
            for {
              ref <- Deferred[F, Either[Throwable, Unit]]
              _   <- Async[F].delay(cli.pullImageCmd(image.show).exec(pullImageCallback[F](ref)))
              _   <- config.pullImageTimeout.fold(ref.get)(t => ref.get.timeout(t))
            } yield ()

          case BuildImage(dockerFile, name, imageLabels) =>
            val labels = (config.defaultLabels ++ imageLabels).map(_.asTuple).toMap.asJava
            for {
              ref <- Deferred[F, Either[Throwable, Unit]]
              df <- Async[F].delay(
                new File(getClass.getClassLoader.getResource(dockerFile).toURI)
              )
              _ <- Async[F].delay(
                cli
                  .buildImageCmd(df)
                  .withDockerfilePath(dockerFile)
                  .withTags(Set(name.show).asJava)
                  .withLabels(labels)
                  .exec(buildImageCallback(ref))
              )
              _ <- config.buildImageTimeout.fold(ref.get)(t => ref.get.timeout(t)).rethrow
            } yield ()
          case PushImage(image) =>
            for {
              ref <- Deferred[F, Either[Throwable, Unit]]
              _   <- Async[F].delay(cli.pushImageCmd(image.show).exec(pushImageCallback[F](ref)))
              _   <- config.pushImageTimeout.fold(ref.get)(t => ref.get.timeout(t)).rethrow
            } yield ()
          case RemoveContainer(c) =>
            for {
              cmd <- Async[F].pure(cli.removeContainerCmd(c.value))
              _   <- Async[F].delay(cmd.exec())
            } yield ()
          case BuildContainer(containerDef) =>
            for {
              cmd       <- Async[F].pure(buildContainerCommand(cli, config.defaultLabels, containerDef))
              container <- Async[F].delay(cmd.exec())
            } yield ContainerId(container.getId)

          case StartContainer(cId) =>
            for {
              _ <- Async[F].delay(cli.startContainerCmd(cId.value).exec())
            } yield ()

          case KillContainer(container) =>
            for {
              _ <- Async[F].delay(cli.killContainerCmd(container.value).exec())
            } yield ()

          case GetContainerDetails(cId) =>
            Async[F]
              .delay(Try(cli.inspectContainerCmd(cId.value).exec()).toEither)
              .map(_.map(x => Some(x)).recoverWith { case _: NotFoundException => Right(None) })
              .rethrow
              .flatMap { d =>
                d.fold(Async[F].pure[Option[ContainerDetails]](None))(details => Async[F].fromEither(ContainerDetails.fromInternalDetails(details).map(Some(_))))
              }

          case GetImageDetailsByRepoAndTag(image) =>
            Async[F]
              .delay(cli.listImagesCmd.withImageNameFilter(image.show).exec())
              .map(r => Option(r).toList.flatMap(_.asScala.toList))
              .map(_.find(img => Option(img.getRepoTags).toList.flatMap(_.toList).contains(image.show)))
              .map(_.map(i => ImageId(i.getId)))

          case GetImageDetails(imgId) =>
            Async[F]
              .delay(Try(cli.inspectImageCmd(imgId.value).exec()).toEither)
              .map(_.map(x => Some(x)).recoverWith { case _: NotFoundException => Right(None) })
              .rethrow
              .flatMap { d =>
                d.fold(Async[F].pure[Option[ImageDetails]](None))(details => Async[F].fromEither(ImageDetails.fromInternalDetails(details).map(Some(_))))
              }
        }
    }

  def resourceInterpreter[F[_]: ConcurrentEffect: Timer](cli: InternalDockerClient, config: Docker4SConfig): FunctionK[DockerAction, Resource[F, *]] = {
    val effInterpreter = effectInterpreter(cli, config)
    def registerShutdownHook(hook: F[_]): F[Thread] =
      Async[F].delay {
        val thread = new Thread(() => hook.toIO.unsafeRunAsyncAndForget())
        Runtime.getRuntime.addShutdownHook(thread)
        thread
      }
    def removeShutdownHook(hook: Thread): F[Unit] =
      Async[F].delay {
        Runtime.getRuntime.removeShutdownHook(hook)
      }

    new (DockerAction ~> Resource[F, *]) {
      import DockerAction._
      override def apply[A](fa: DockerAction[A]): Resource[F, A] =
        fa match {
          case build: BuildContainer =>
            Resource
              .make {
                for {
                  cId <- effInterpreter(build)
                  removeAction = removeContainerIfExists[F](cId).foldMap(effInterpreter)
                  hook <- registerShutdownHook(removeAction)
                } yield (cId, hook)
              }({
                case (cId, hook) =>
                  for {
                    _ <- removeContainerIfExists[F](cId).foldMap(effInterpreter)
                    _ <- removeShutdownHook(hook)
                  } yield ()
              })
              .map(_._1)
          case start: StartContainer =>
            Resource
              .make(
                (for {
                  _ <- effInterpreter(start)
                  killAction = killContainerIfRunning[F](start.container).foldMap(effInterpreter)
                  hook <- registerShutdownHook(killAction)
                } yield hook)
              )(hook =>
                for {
                  _ <- killContainerIfRunning[F](start.container).foldMap(effInterpreter)
                  _ <- removeShutdownHook(hook)
                } yield ()
              )
              .void
          case other => Resource.liftF[F, A](effInterpreter(other))
        }
    }
  }

  private def buildContainerCommand(cli: InternalDockerClient, defaultLabels: List[Label], containerDef: ContainerDef): CreateContainerCmd = {
    val labels: Map[String, String] = (containerDef.labels ++ defaultLabels).map(_.asTuple).toMap
    val command = cli
      .createContainerCmd(containerDef.sourceImage.show)
      .withEnv(containerDef.envVars.map(_.show).asJava)
      .withExposedPorts(
        containerDef.exposedPort.map(p => new ExposedPort(p.port, InternetProtocol.asInternalProtocol(p.protocol))): _*
      )
      .withLabels(labels.asJava)
      .withHostConfig(new HostConfig().withPublishAllPorts(true))
    Util.builderApply[String, CreateContainerCmd](containerDef.name)(_.withName)(command)
  }

  private def pullImageCallback[F[_]: Effect](ref: Deferred[F, Either[Throwable, Unit]]): PullImageResultCallback =
    new PullImageResultCallback() { self =>
      override def onError(throwable: Throwable): Unit =
        ref.complete(throwable.asLeft).toIO.unsafeRunAsyncAndForget()
      override def onComplete(): Unit =
        ref.complete(().asRight).toIO.unsafeRunAsyncAndForget()
    }

  private def pushImageCallback[F[_]: Effect](ref: Deferred[F, Either[Throwable, Unit]]): ResultCallback.Adapter[PushResponseItem] =
    new ResultCallback.Adapter[PushResponseItem]() { self =>
      override def onError(throwable: Throwable): Unit =
        ref.complete(throwable.asLeft).toIO.unsafeRunAsyncAndForget()
      override def onComplete(): Unit =
        ref.complete(().asRight).toIO.unsafeRunAsyncAndForget()
    }

  private def buildImageCallback[F[_]: Effect](ref: Deferred[F, Either[Throwable, Unit]]): BuildImageResultCallback =
    new BuildImageResultCallback() { self =>
      override def onError(throwable: Throwable): Unit =
        ref.complete(throwable.asLeft).toIO.unsafeRunAsyncAndForget()
      override def onComplete(): Unit =
        ref.complete(().asRight).toIO.unsafeRunAsyncAndForget()
    }

}
