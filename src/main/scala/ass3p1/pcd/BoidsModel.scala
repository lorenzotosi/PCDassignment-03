package ass3p1.pcd

import pcd.ass01.{P2d, V2d}

import scala.collection.mutable.ListBuffer

class BoidsModel(nboids: Int,
                 initialSeparationWeight: Double,
                 initialAlignmentWeight: Double,
                 initialCohesionWeight: Double,
                 width: Double,
                 height: Double,
                 maxSpeed: Double,
                 perceptionRadius: Double,
                 avoidRadius: Double) {

  private val boids: ListBuffer[Boid] = ListBuffer()
  private var separationWeight: Double = initialSeparationWeight
  private var alignmentWeight: Double = initialAlignmentWeight
  private var cohesionWeight: Double = initialCohesionWeight

  def getBoids: List[Boid] = boids.toList

  def getMinX: Double = -width / 2

  def getMaxX: Double = width / 2

  def getMinY: Double = -height / 2

  def getMaxY: Double = height / 2

  def getWidth: Double = width

  def getHeight: Double = height

  def setSeparationWeight(value: Double): Unit = {
    this.separationWeight = value
  }

  def setAlignmentWeight(value: Double): Unit = {
    this.alignmentWeight = value
  }

  def setCohesionWeight(value: Double): Unit = {
    this.cohesionWeight = value
  }

  def getSeparationWeight: Double = separationWeight

  def getCohesionWeight: Double = cohesionWeight

  def getAlignmentWeight: Double = alignmentWeight

  def getMaxSpeed: Double = maxSpeed

  def getAvoidRadius: Double = avoidRadius

  def getPerceptionRadius: Double = perceptionRadius

  // Initialize boids
  for (_ <- 0 until nboids) {
    val pos = new P2d(-width / 2 + Math.random() * width, -height / 2 + Math.random() * height)
    val vel = new V2d(Math.random() * maxSpeed / 2 - maxSpeed / 4, Math.random() * maxSpeed / 2 - maxSpeed / 4)
    boids += new Boid(pos, vel)
  }
}