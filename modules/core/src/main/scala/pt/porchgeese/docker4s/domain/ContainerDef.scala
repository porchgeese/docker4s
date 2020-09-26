package pt.porchgeese.docker4s.domain

final case class ContainerDef(sourceImage: ImageName, envVars: List[EnvVar], exposedPort: List[ExposedPort], name: Option[String] = None)
