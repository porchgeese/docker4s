package pt.porchgeese.resource

import cats.effect.IO.Async
import cats.effect.{Async, ConcurrentEffect, IO, LiftIO, Resource, Timer}
import pt.porchgeese.containers4s.Containers4S.Docker4SUserConfig
import pt.porchgeese.containers4s.adt.DockerActionSyntax
import pt.porchgeese.containers4s.docker.DockerClient
import pt.porchgeese.containers4s.domain.{ContainerDef, ContainerDetails}
import pt.porchgeese.containers4s.interpreters.DockerActionInterpreter


