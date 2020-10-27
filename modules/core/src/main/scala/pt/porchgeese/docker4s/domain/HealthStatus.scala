package pt.porchgeese.docker4s.domain

object HealthStatus {
  sealed trait HealthStatus extends Product with Serializable
  final case object Healthy   extends HealthStatus
  final case object Unhealthy extends HealthStatus
  final case object Aborted   extends HealthStatus
}
