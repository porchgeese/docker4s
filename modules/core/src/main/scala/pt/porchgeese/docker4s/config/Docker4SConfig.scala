package pt.porchgeese.docker4s.config

import java.net.URI

import com.github.dockerjava.core.DefaultDockerClientConfig
import com.github.dockerjava.transport.SSLConfig
import pt.porchgeese.docker4s.config
import pt.porchgeese.docker4s.label.Label
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
    apiVersion: String,
    defaultLabels: List[Label]
)

private[docker4s] object Docker4SConfig {
  private type DockerCliConfBuilder = DefaultDockerClientConfig.Builder

  def buildConfig(userConfig: Docker4SUserConfig = Docker4SUserConfig()): Docker4SConfig = {
    val defaultConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
    val sslContext    = userConfig.sslContext.map(x => new SSLConfig { def getSSLContext = x }).getOrElse(defaultConfig.getSSLConfig)
    val dockerHost    = userConfig.dockerHost.getOrElse(defaultConfig.getDockerHost)
    val apiVersion    = userConfig.apiVersion.getOrElse(defaultConfig.getApiVersion.getVersion)
    config.Docker4SConfig(
      dockerHost = dockerHost,
      sslContext = sslContext,
      pullImageTimeout = userConfig.pullImageTimeout,
      pushImageTimeout = userConfig.pushImageTimeout,
      buildImageTimeout = userConfig.buildImageTimeout,
      registryPassword = userConfig.registryPassword,
      registryUsername = userConfig.registryUsername,
      registryEmail = userConfig.registryEmail,
      registryUrl = userConfig.registryUrl,
      apiVersion = apiVersion,
      defaultLabels = userConfig.labels
    )
  }

}
