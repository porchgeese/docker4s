package pt.porchgeese.docker4s.domain
import com.github.dockerjava.api.model.{InternetProtocol => InternalInternetProtocol}

sealed trait InternetProtocol
object InternetProtocol {
  final case object TCP  extends InternetProtocol
  final case object UDP  extends InternetProtocol
  final case object SCTP extends InternetProtocol

  private[docker4s] def asInternalProtocol(p: InternetProtocol): InternalInternetProtocol =
    p match {
      case TCP  => InternalInternetProtocol.TCP
      case UDP  => InternalInternetProtocol.UDP
      case SCTP => InternalInternetProtocol.SCTP
    }

  private[docker4s] def fromInternalProtocol(p: InternalInternetProtocol): InternetProtocol =
    p match {
      case InternalInternetProtocol.TCP  => TCP
      case InternalInternetProtocol.UDP  => UDP
      case InternalInternetProtocol.SCTP => SCTP
    }
}
