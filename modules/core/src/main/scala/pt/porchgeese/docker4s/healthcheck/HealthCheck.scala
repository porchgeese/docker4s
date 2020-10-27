package pt.porchgeese.docker4s.healthcheck

import cats.Applicative
import cats.effect.{ConcurrentEffect, Resource, Timer}
import pt.porchgeese.docker4s.domain.{HealthCheckConfig, HealthStatus}
import cats.implicits._
import cats.effect.implicits._
import pt.porchgeese.docker4s.domain.HealthStatus.HealthStatus
import pt.porchgeese.docker4s.util.Repeater

object HealthCheck {

  def eval[F[_]: ConcurrentEffect: Timer](check: F[HealthStatus], config: HealthCheckConfig): F[HealthStatus] = {
    val checkCode = Repeater.repeatUntilRightWithCount[F, HealthStatus](
      check,
      {
        case (status, _) if status != HealthStatus.Unhealthy                     => status.asRight
        case (_, cnt) if config.globalTerminationCriteria.right.exists(_ <= cnt) => HealthStatus.Aborted.asRight
        case _                                                                   => ().asLeft
      }
    )
    config.globalTerminationCriteria.left.fold(checkCode)(f => checkCode.timeoutTo(f, Applicative[F].pure(HealthStatus.Aborted)))
  }

  def evalRes[F[_]: ConcurrentEffect: Timer](check: F[HealthStatus], config: HealthCheckConfig): Resource[F, HealthStatus] =
    Resource.liftF(eval(check, config))
}
