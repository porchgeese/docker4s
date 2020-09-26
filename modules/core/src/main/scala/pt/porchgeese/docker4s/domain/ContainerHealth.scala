package pt.porchgeese.docker4s.domain

object ContainerHealth {
  sealed trait ContainerHealth

  case object STARTING  extends ContainerHealth
  case object HEALTHY   extends ContainerHealth
  case object UNHEALTHY extends ContainerHealth
  case object NONE      extends ContainerHealth

  def fromInternal(s: String): ContainerHealth =
    s.toUpperCase match {
      case "STARTING"  => STARTING
      case "HEALTHY"   => HEALTHY
      case "UNHEALTHY" => UNHEALTHY
      case "NONE"      => NONE
    }
}
