package pt.porchgeese.docker4s.util

private[docker4s] object Util {
  def builderApply[A, B](config: Option[A])(f: B => A => B): B => B =
    (b: B) => config.fold(b)(conf => f(b)(conf))
}
