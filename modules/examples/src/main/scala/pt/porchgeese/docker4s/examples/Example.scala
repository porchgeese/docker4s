package pt.porchgeese.docker4s.examples

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import pt.porchgeese.docker4s.Docker4SClient
import pt.porchgeese.docker4s.domain.{ContainerDef, EnvVar, ImageName}
import pt.porchgeese.docker4s.implicits._

import scala.concurrent.duration.DurationInt

object Example extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    Docker4SClient
      .buildDockerResourceClient[IO]()
      .flatMap { cli =>
        for {
          containers <- (1 to 20).toList.map(_ => cli.buildContainer(ContainerDef(ImageName("postgres", "alpine"), List(EnvVar("POSTGRES_PASSWORD", "admin")), Nil))).sequence
          _          <- containers.map(cli.startContainer(_)).sequence
        } yield ()
      }
      .use(_ => IO())
      .map(_ => ExitCode.Success)

  def loopUntilTrue(f: IO[Boolean]): IO[Boolean] =
    f.flatMap { res =>
      IO.sleep(2.seconds).flatMap(_ => if (res) IO(true) else f)
    }
}
