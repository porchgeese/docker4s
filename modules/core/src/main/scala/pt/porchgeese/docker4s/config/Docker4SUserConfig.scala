package pt.porchgeese.docker4s.config

import java.net.URI
import javax.net.ssl.SSLContext
import scala.concurrent.duration.FiniteDuration

case class Docker4SUserConfig(
    dockerHost: Option[URI] = None,
    sslContext: Option[SSLContext] = None,
    pullImageTimeout: Option[FiniteDuration] = None,
    pushImageTimeout: Option[FiniteDuration] = None,
    buildImageTimeout: Option[FiniteDuration] = None,
    registryPassword: Option[String] = None,
    registryUsername: Option[String] = None,
    registryEmail: Option[String] = None,
    registryUrl: Option[String] = None,
    apiVersion: Option[String] = None
)
