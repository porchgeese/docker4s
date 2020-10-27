package pt.porchgeese.docker4s.domain

import pt.porchgeese.docker4s.domain.HealthStatus.HealthStatus

final case class HealthCheckDef[F[_]](
    check: F[HealthStatus],
    config: HealthCheckConfig
)
