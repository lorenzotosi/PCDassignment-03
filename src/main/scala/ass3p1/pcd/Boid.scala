package ass3p1.pcd

import pcd.ass01.{P2d, V2d}

class Boid(var pos: P2d, var vel: V2d) {

  def getPos: P2d = pos

  def getVel: V2d = vel

  def updateVelocity(model: BoidsModel): Unit = {
    val nearbyBoids = getNearbyBoids(model)

    val separation = calculateSeparation(nearbyBoids, model)
    val alignment = calculateAlignment(nearbyBoids, model)
    val cohesion = calculateCohesion(nearbyBoids, model)

    vel = vel.sum(alignment.mul(model.getAlignmentWeight))
      .sum(separation.mul(model.getSeparationWeight))
      .sum(cohesion.mul(model.getCohesionWeight))
    val speed = vel.abs()

    if (speed > model.getMaxSpeed) {
      vel = vel.getNormalized().mul(model.getMaxSpeed)
    }
  }

  def updatePos(model: BoidsModel): Unit = {
    pos = pos.sum(vel)
    if (pos.x < model.getMinX) pos = pos.sum(new V2d(model.getWidth, 0))
    if (pos.x >= model.getMaxX) pos = pos.sum(new V2d(-model.getWidth, 0))
    if (pos.y < model.getMinY) pos = pos.sum(new V2d(0, model.getHeight))
    if (pos.y >= model.getMaxY) pos = pos.sum(new V2d(0, -model.getHeight))
  }

  private def getNearbyBoids(model: BoidsModel): List[Boid] = {
    model.getBoids.filter(other => other != this && pos.distance(other.getPos) < model.getPerceptionRadius)
  }

  private def calculateAlignment(nearbyBoids: List[Boid], model: BoidsModel): V2d = {
    if (nearbyBoids.nonEmpty) {
      val (avgVx, avgVy) = nearbyBoids.foldLeft((0.0, 0.0)) { case ((vx, vy), other) =>
        val otherVel = other.getVel
        (vx + otherVel.x, vy + otherVel.y)
      }
      val size = nearbyBoids.size
      val normalized = new V2d(avgVx / size - vel.x, avgVy / size - vel.y).getNormalized()
      normalized
    } else {
      new V2d(0, 0)
    }
  }

  private def calculateCohesion(nearbyBoids: List[Boid], model: BoidsModel): V2d = {
    if (nearbyBoids.nonEmpty) {
      val (centerX, centerY) = nearbyBoids.foldLeft((0.0, 0.0)) { case ((cx, cy), other) =>
        val otherPos = other.getPos
        (cx + otherPos.x, cy + otherPos.y)
      }
      val size = nearbyBoids.size
      val normalized = new V2d(centerX / size - pos.x, centerY / size - pos.y).getNormalized()
      normalized
    } else {
      new V2d(0, 0)
    }
  }

  private def calculateSeparation(nearbyBoids: List[Boid], model: BoidsModel): V2d = {
    val (dx, dy, count) = nearbyBoids.foldLeft((0.0, 0.0, 0)) { case ((dx, dy, count), other) =>
      val otherPos = other.getPos
      val distance = pos.distance(otherPos)
      if (distance < model.getAvoidRadius) {
        (dx + (pos.x - otherPos.x), dy + (pos.y - otherPos.y), count + 1)
      } else {
        (dx, dy, count)
      }
    }
    if (count > 0) {
      new V2d(dx / count, dy / count).getNormalized()
    } else {
      new V2d(0, 0)
    }
  }
}