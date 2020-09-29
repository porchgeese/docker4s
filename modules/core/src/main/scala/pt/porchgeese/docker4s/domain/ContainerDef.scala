package pt.porchgeese.docker4s.domain

import pt.porchgeese.docker4s.label.Label

final case class ContainerDef(
    sourceImage: ImageName,
    envVars: List[EnvVar],
    exposedPort: List[ExposedPort],
    name: Option[String] = None,
    labels: List[Label] = Nil
)
