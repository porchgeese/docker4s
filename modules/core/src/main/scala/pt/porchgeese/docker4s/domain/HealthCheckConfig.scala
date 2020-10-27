package pt.porchgeese.docker4s.domain

import cats.data.Ior
import pt.porchgeese.docker4s.domain.HealthStatus.HealthStatus

import scala.concurrent.duration.{FiniteDuration, _}

final case class HealthCheckConfig(
    globalTerminationCriteria: Ior[FiniteDuration, Int],
    poolingSleepTime: FiniteDuration = 300.milliseconds
)
