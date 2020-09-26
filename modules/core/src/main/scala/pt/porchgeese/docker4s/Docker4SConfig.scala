package pt.porchgeese.docker4s

import java.net.URI

import com.github.dockerjava.transport.SSLConfig

import scala.concurrent.duration.FiniteDuration

private[docker4s] case class Docker4SConfig(
    dockerHost: URI,
    sslContext: SSLConfig,
    pullImageTimeout: Option[FiniteDuration],
    pushImageTimeout: Option[FiniteDuration],
    buildImageTimeout: Option[FiniteDuration],
    registryPassword: Option[String],
    registryUsername: Option[String],
    registryEmail: Option[String],
    registryUrl: Option[String],
    apiVersion: String
)
