package pt.porchgeese.docker4s.domain

object ContainerHealth {
  sealed trait ContainerHealth

  final case object STARTING  extends ContainerHealth
  final case object HEALTHY   extends ContainerHealth
  final case object UNHEALTHY extends ContainerHealth
  final case object NONE      extends ContainerHealth

  def fromInternal(s: String): ContainerHealth =
    s.toUpperCase match {
      case "STARTING"  => STARTING
      case "HEALTHY"   => HEALTHY
      case "UNHEALTHY" => UNHEALTHY
      case "NONE"      => NONE
    }
}
