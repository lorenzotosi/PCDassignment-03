package ass3p1.pcd

import akka.actor.typed.Scheduler
import akka.actor.typed.scaladsl.AskPattern.*
import akka.util.Timeout
import ass3p1.MySystem
import ass3p1.MySystem.Command.Execute

import scala.concurrent.Await
import scala.concurrent.duration.*
import scala.util.Try

class BoidsSimulator(model: BoidsModel) {

  private var view: Option[BoidsView] = None

  private val FRAMERATE = 100
  private var framerate: Int = 0

  def attachView(view: BoidsView): Unit = {
    this.view = Some(view)
  }

  def runSimulation(): Unit = {
    implicit val timeout: Timeout = 1.seconds
    implicit val scheduler: Scheduler = model.actor.scheduler

    while (true) {
      val t0 = System.currentTimeMillis()

      val future = model.actor ? (ref => Execute(ref))
      try {
        Await.result(future, timeout.duration)
      } catch {
        case e: Exception => e.printStackTrace()
      }

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