package pt.porchgeese.docker4s.domain

import pt.porchgeese.docker4s.label.Label

sealed trait ContainerDef {
  def sourceImage: ImageName
  def envVars: List[EnvVar]
  def exposedPort: List[ExposedPort]
  def name: Option[String]
  def labels: List[Label]
}

final case class SimpleContainerDef(
    sourceImage: ImageName,
    envVars: List[EnvVar],
    exposedPort: List[ExposedPort],
    name: Option[String],
    labels: List[Label]
) extends ContainerDef

final case class WithHealthCheckContainerdDef[F[_]](
    sourceImage: ImageName,
    envVars: List[EnvVar],
    exposedPort: List[ExposedPort],
    name: Option[String] = None,
    labels: List[Label],
    healthCheck: HealthCheckDef[F]
) extends ContainerDef

object ContainerDef {
  class ContainerDefWithHealthcheck
  def simple(
      sourceImage: ImageName,
      envVars: List[EnvVar],
      exposedPort: List[ExposedPort],
      name: Option[String] = None,
      labels: List[Label] = Nil
  ): SimpleContainerDef =
    SimpleContainerDef(sourceImage, envVars, exposedPort, name, labels)

  def withHealthCheck[F[_]](
      containerDef: ContainerDef,
      healthCheck: HealthCheckDef[F]
  ): WithHealthCheckContainerdDef[F] =
    WithHealthCheckContainerdDef[F](containerDef.sourceImage, containerDef.envVars, containerDef.exposedPort, containerDef.name, containerDef.labels, healthCheck)

  def withHealthCheck[F[_]](
      sourceImage: ImageName,
      envVars: List[EnvVar],
      exposedPort: List[ExposedPort],
      healthCheck: HealthCheckDef[F],
      name: Option[String] = None,
      labels: List[Label] = Nil
  ): WithHealthCheckContainerdDef[F] =
    WithHealthCheckContainerdDef[F](sourceImage, envVars, exposedPort, name, labels, healthCheck)
}
