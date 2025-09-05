package ass3p1.pcd

import javax.swing._
import java.awt._

class BoidsPanel(view: BoidsView, model: BoidsModel) extends JPanel {

  private var framerate: Int = _

  def setFrameRate(framerate: Int): Unit = {
    this.framerate = framerate
  }

  override protected def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    setBackground(Color.WHITE)

    val w = view.getWidth
    val h = view.getHeight
    val envWidth = model.getWidth
    val xScale = w.toDouble / envWidth

    val boids = model.getBoids

    g.setColor(Color.BLUE)
    for (boid <- boids) {
      val x = boid.getPos.x()
      val y = boid.getPos.y()
      val px = (w / 2 + x * xScale).toInt
      val py = (h / 2 - y * xScale).toInt
      g.fillOval(px, py, 5, 5)
    }

    g.setColor(Color.BLACK)
    g.drawString(s"Num. Boids: ${boids.size}", 10, 25)
    g.drawString(s"Framerate: $framerate", 10, 40)
  }
}