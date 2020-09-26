package pt.porchgeese.docker4s

import cats.arrow.FunctionK
import cats.effect.{Async, ConcurrentEffect, Resource, Sync, Timer}
import cats.{Applicative, ApplicativeError}
import com.github.dockerjava.core.{DefaultDockerClientConfig, DockerClientConfig, DockerClientImpl}
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient
import com.github.dockerjava.transport.SSLConfig
import pt.porchgeese.docker4s.interpreters.Interpreter
import pt.porchgeese.docker4s.util.Util
import cats.implicits._
import com.github.dockerjava.api.{DockerClient => InternalDockerClient}
import pt.porchgeese.docker4s.adt.DockerAction

import scala.util.Try

object Docker4SClient {
  private type HttpCliBuilder       = ApacheDockerHttpClient.Builder
  private type DockerCliConfBuilder = DefaultDockerClientConfig.Builder
  type Docker4SClient[F[_]]         = FunctionK[DockerAction, F]
  type ResourceDocker4SClient[F[_]] = FunctionK[DockerAction, Resource[F, *]]

  def buildDockerClient[F[_]: ConcurrentEffect: Timer](userConfigs: Docker4SUserConfig = Docker4SUserConfig()): Resource[F, Docker4SClient[F]] =
    for {
      configWithDetails <- Resource.liftF(Applicative[F].pure(buildConfig(userConfigs)))
      config            <- Resource.liftF(Async[F].fromEither(buildDockerClientConfig[F](configWithDetails)))
      httpCli           <- buildHttClient[F](configWithDetails)
      client            <- buildDockerClient[F](httpCli, config)
      intrptr = Interpreter.effectInterpreter[F](client, configWithDetails)
    } yield intrptr

  def buildDockerResourceClient[F[_]: ConcurrentEffect: Timer](userConfigs: Docker4SUserConfig = Docker4SUserConfig()): Resource[F, ResourceDocker4SClient[F]] =
    for {
      configWithDetails <- Resource.liftF(Applicative[F].pure(buildConfig(userConfigs)))
      config            <- Resource.liftF(Async[F].fromEither(buildDockerClientConfig[F](configWithDetails)))
      httpCli           <- buildHttClient[F](configWithDetails)
      client            <- buildDockerClient[F](httpCli, config)
      intrptr = Interpreter.resourceInterpreter[F](client, configWithDetails)
    } yield intrptr

  private def buildDockerClientConfig[F[_]](config: Docker4SConfig)(implicit ME: ApplicativeError[F, Throwable]): Either[Throwable, DefaultDockerClientConfig] =
    Try(
      List(
        Util.builderApply[String, DockerCliConfBuilder](config.registryPassword)(_.withRegistryPassword),
        Util.builderApply[String, DockerCliConfBuilder](config.registryUsername)(_.withRegistryUsername),
        Util.builderApply[String, DockerCliConfBuilder](config.registryEmail)(_.withRegistryEmail),
        Util.builderApply[String, DockerCliConfBuilder](config.registryUrl)(_.withRegistryUrl)
      ).foldLeft(DefaultDockerClientConfig.createDefaultConfigBuilder())((f, b) => b(f))
        .withApiVersion(config.apiVersion)
        .build()
    ).toEither

  private def buildDockerClient[F[_]: Async](client: ApacheDockerHttpClient, clientConfig: DockerClientConfig): Resource[F, InternalDockerClient] =
    Resource.make(
      Sync[F]
        .delay(Try(DockerClientImpl.getInstance(clientConfig, client)).toEither)
        .rethrow
    )(r => Async[F].delay(Try(r.close()).toEither).rethrow)

  private def buildHttClient[F[_]: Async](config: Docker4SConfig): Resource[F, ApacheDockerHttpClient] =
    Resource.make[F, ApacheDockerHttpClient](Sync[F].delay {
      (new ApacheDockerHttpClient.Builder)
        .dockerHost(config.dockerHost)
        .sslConfig(config.sslContext)
        .build()
    })(cli => Async[F].delay(cli.close()))

  private def buildConfig(userConfig: Docker4SUserConfig = Docker4SUserConfig()): Docker4SConfig = {
    val defaultConfig = DefaultDockerClientConfig.createDefaultConfigBuilder().build()
    val sslContext    = userConfig.sslContext.map(x => new SSLConfig { def getSSLContext = x }).getOrElse(defaultConfig.getSSLConfig)
    val dockerHost    = userConfig.dockerHost.getOrElse(defaultConfig.getDockerHost)
    val apiVersion    = userConfig.apiVersion.getOrElse(defaultConfig.getApiVersion.getVersion)
    Docker4SConfig(
      dockerHost = dockerHost,
      sslContext = sslContext,
      pullImageTimeout = userConfig.pullImageTimeout,
      pushImageTimeout = userConfig.pushImageTimeout,
      buildImageTimeout = userConfig.buildImageTimeout,
      registryPassword = userConfig.registryPassword,
      registryUsername = userConfig.registryUsername,
      registryEmail = userConfig.registryEmail,
      registryUrl = userConfig.registryUrl,
      apiVersion = apiVersion
    )
  }

}
