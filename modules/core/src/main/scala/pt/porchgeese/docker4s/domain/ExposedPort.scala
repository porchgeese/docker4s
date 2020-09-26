package pt.porchgeese.docker4s.domain

import pt.porchgeese.docker4s
import com.github.dockerjava.api.model.{ExposedPort => InternalExposedPort}

final case class ExposedPort(port: Int, protocol: InternetProtocol = InternetProtocol.TCP)

object ExposedPort {
  def fromInternalExposedPort(ep: InternalExposedPort): ExposedPort =
    ExposedPort(ep.getPort, InternetProtocol.fromInternalProtocol(ep.getProtocol))
}
