package pt.porchgeese.docker4s.util

import cats.Applicative
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._

object Repeater {
  def repeatUntilRightWithCount[F[_]: Sync, A](f: F[A], until: ((A, Int)) => Either[Unit, A]): F[A] =
    for {
      ref <- Ref.of[F, Int](0)
      result <- (for {
          cnt    <- ref.getAndUpdate(_ + 1)
          result <- f
          continue = until(result -> cnt)
        } yield continue).iterateUntil(e => e.isRight)
    } yield result.toOption.get //save since left can't ever be returned
}
