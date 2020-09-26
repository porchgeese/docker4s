package pt.porchgeese.docker4s.domain

object ContainerStatus {
  sealed trait ContainerStatus extends Serializable with Product
  final case object CREATED    extends ContainerStatus
  final case object RESTARTING extends ContainerStatus
  final case object RUNNING    extends ContainerStatus
  final case object REMOVING   extends ContainerStatus
  final case object PAUSED     extends ContainerStatus
  final case object EXITED     extends ContainerStatus
  final case object DEAD       extends ContainerStatus
  val runningStatuses = List(RESTARTING, RUNNING, REMOVING)
  def fromInternal(s: String): ContainerStatus =
    s.toUpperCase match {
      case "CREATED"    => CREATED
      case "RESTARTING" => RESTARTING
      case "RUNNING"    => RUNNING
      case "REMOVING"   => REMOVING
      case "PAUSED"     => PAUSED
      case "EXITED"     => EXITED
      case "DEAD"       => DEAD
    }
}
