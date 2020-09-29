package pt.porchgeese.docker4s.label

object Docker4sLabels {
  val Docker4S: Label             = Label("pt.porchgeese.docker4s", "true")
  def run(runId: String): Label   = Label("pt.porchgeese.docker4s.run", runId)
  def run(timestamp: Long): Label = Label("pt.porchgeese.docker4s.creation", timestamp.toString)
}
