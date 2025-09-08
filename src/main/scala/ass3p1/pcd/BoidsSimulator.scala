package ass3p1.pcd

import scala.util.Try

class BoidsSimulator(model: BoidsModel) {

  private var view: Option[BoidsView] = None

  private val FRAMERATE = 100
  private var framerate: Int = 0

  def attachView(view: BoidsView): Unit = {
    this.view = Some(view)
  }

  def runSimulation(): Unit = {
    while (true) {
      val t0 = System.currentTimeMillis()

      model.actor ! ass3p1.MySystem.Command.Execute

      view.foreach(_.update(framerate))
      val t1 = System.currentTimeMillis()
      val dtElapsed = t1 - t0
      val framratePeriod = 1000 / FRAMERATE

      if (dtElapsed < framratePeriod) {
        Try(Thread.sleep(framratePeriod - dtElapsed))
        framerate = FRAMERATE
      } else {
        framerate = (1000 / dtElapsed).toInt
      }
    }
  }
}