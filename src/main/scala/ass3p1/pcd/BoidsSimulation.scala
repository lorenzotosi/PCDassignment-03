package ass3p1.pcd

object BoidsSimulation {

  final val N_BOIDS = 1500

  final val SEPARATION_WEIGHT = 1.0
  final val ALIGNMENT_WEIGHT = 1.0
  final val COHESION_WEIGHT = 1.0

  final val ENVIRONMENT_WIDTH = 1000
  final val ENVIRONMENT_HEIGHT = 1000
  final val MAX_SPEED = 4.0
  final val PERCEPTION_RADIUS = 50.0
  final val AVOID_RADIUS = 20.0

  final val SCREEN_WIDTH = 800
  final val SCREEN_HEIGHT = 800

  def main(args: Array[String]): Unit = {
    val model = new BoidsModel(
      N_BOIDS,
      SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
      ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
      MAX_SPEED,
      PERCEPTION_RADIUS,
      AVOID_RADIUS
    )
    val sim = new BoidsSimulator(model)
    val view = new BoidsView(model, SCREEN_WIDTH, SCREEN_HEIGHT)
    sim.attachView(view)
    sim.runSimulation()
  }
}