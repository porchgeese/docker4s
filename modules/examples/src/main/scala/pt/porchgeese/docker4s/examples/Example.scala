package pt.porchgeese.docker4s.examples

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import pt.porchgeese.docker4s.Docker4SClient
import pt.porchgeese.docker4s.domain.{ContainerDef, EnvVar, ImageName}
import pt.porchgeese.docker4s.implicits._

import scala.concurrent.duration.DurationInt

object Example extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val postgres = ContainerDef(ImageName("postgres", "alpine"), List(EnvVar("POSTGRES_PASSWORD", "admin")), Nil)
    Docker4SClient
      .buildDockerResourceClient[IO]()
      .flatMap { cli =>
        for {
          containers <- (1 to 20).toList.map(_ => cli.buildContainer(postgres)).sequence
          _          <- containers.map(cli.startContainer(_)).sequence
          details    <- Resource.liftF(containers.map(cli.getContainerDetails).sequence)
          _ = println(details.flatMap(_.toList).mkString("\n"))
        } yield ()
      }
      .use(_ => IO())
      .map(_ => ExitCode.Success)
  }

  def loopUntilTrue(f: IO[Boolean]): IO[Boolean] =
    f.flatMap { res =>
      IO.sleep(2.seconds).flatMap(_ => if (res) IO(true) else f)
    }
}
