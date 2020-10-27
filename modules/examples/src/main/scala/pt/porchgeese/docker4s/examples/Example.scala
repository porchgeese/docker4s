package pt.porchgeese.docker4s.examples

import cats.data.Ior
import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import doobie.Transactor
import pt.porchgeese.docker4s.Docker4SClient
import pt.porchgeese.docker4s.domain.{ContainerDef, EnvVar, ExposedPort, HealthCheckConfig, HealthCheckDef, HealthStatus, ImageName}
import pt.porchgeese.docker4s.healthcheck.HealthCheck
import pt.porchgeese.docker4s.implicits._
import doobie.implicits._
import doobie.syntax._
import pt.porchgeese.docker4s.domain.HealthStatus.{Aborted, HealthStatus, Healthy, Unhealthy}

import scala.concurrent.duration.DurationInt

object Example extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val postgres = ContainerDef.simple(
      ImageName("postgres", "alpine"),
      List(EnvVar("POSTGRES_PASSWORD", "admin")),
      List(ExposedPort(5432))
    )
    val defaultHealthConfig = HealthCheckConfig(Ior.both(20.seconds, 10))
    (for {
      cli       <- Docker4SClient.buildDockerResourceClient[IO]()
      container <- cli.buildContainer(postgres)
      _         <- cli.startContainer(container)
      details   <- Resource.liftF(cli.getContainerDetails(container))
      dbPort       = details.get.exposedPorts(ExposedPort(5432)).head.port
      dbConnection = Transactor.fromDriverManager[IO]("org.postgresql.Driver", s"jdbc:postgresql:127.0.0.1:${dbPort}", "postgres", "")
      _ <- HealthCheck.evalRes[IO](postgresHealthCheck(dbConnection), defaultHealthConfig)
    } yield ())
      .use(_ => IO())
      .map(_ => ExitCode.Success)
  }

  def postgresHealthCheck(db: Transactor[IO]): IO[HealthStatus] =
    (sql"SELECT 1;"
      .query[Int]
      .option)
      .transact(db)
      .map(_.as(Healthy).getOrElse(Aborted))
      .handleErrorWith(_ => Unhealthy.pure[IO])

}
