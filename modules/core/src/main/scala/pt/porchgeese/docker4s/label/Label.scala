package pt.porchgeese.docker4s.label

case class Label(k: String, v: String) {
  def asTuple: (String, String) = (k, v)
}
