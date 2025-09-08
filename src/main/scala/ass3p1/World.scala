package ass3p1

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import ass3p1.World.Operations.*
import ass3p1.pcd.BoidsModel

object MySystem:
  enum Command:
    case StartWorld(nBoids: Int, boids: List[pcd.Boid])
    case RestartWorld(nBoids: Int)
    case Execute
    case StopWorld

  import Command.*

  def apply(model: BoidsModel): Behavior[Command] =
    Behaviors.setup { ctx =>
      var currentWorld: Option[ActorRef[World.Operations]] = None

      Behaviors.receiveMessage {
        case StartWorld(n, list) =>
          ctx.log.info(s"Starting World with $n boids")
          val world = ctx.spawn(World(n), "World")
          currentWorld = Some(world)
          Behaviors.same

        case Execute =>
          if currentWorld.isDefined then
            currentWorld.get ! World.Operations.Update(model)
          Behaviors.same

        case RestartWorld(n) =>
          ctx.log.info(s"Restarting World with $n boids")
          currentWorld.foreach { w =>
            ctx.stop(w)
          }
          val newWorld = ctx.spawn(World(n), "WorldNew_" + System.nanoTime())
          currentWorld = Some(newWorld)
          Behaviors.same

        case StopWorld =>
          ctx.log.info("Stopping World")
          currentWorld.foreach(ctx.stop)
          currentWorld = None
          Behaviors.same
      }
    }


object World:
  enum Operations:
    case Start
    case Stop
    case Update(model: BoidsModel)
    case Ack(from: Int)

  export Operations.*

  def apply(nBoids: Int): Behavior[Operations] =
    Behaviors.setup { ctx =>
      val group = if nBoids % 50 != 0 then nBoids / 50 + 1 else nBoids / 50

      val boidActors: Seq[ActorRef[BoidActor.Command]] =
        for i <- 0 until group yield
          ctx.spawn(BoidActor(i), s"dummy${i}_${System.nanoTime()}")

      def idle: Behavior[Operations] =
        Behaviors.receiveMessage {
          case Start =>
            ctx.log.info(s"World($nBoids) started")
            Behaviors.same
          case Stop =>
            ctx.log.info(s"World($nBoids) stopped")
            Behaviors.stopped
          case Update(model) =>
            boidActors.zipWithIndex.foreach { case (actor, id) =>
              actor ! BoidActor.DoUpdate(ctx.self, id, model)
            }
            waitingAcks(expected = boidActors.size, received = 0)
          case Ack(from) =>
            ctx.log.warn(s"Unexpected ACK from Dummy $from (not waiting)")
            Behaviors.same
        }

      def waitingAcks(expected: Int, received: Int): Behavior[Operations] =
        Behaviors.receiveMessage {
          case Ack(from) =>
            val newCount = received + 1
            if newCount == expected then
              idle
            else
              waitingAcks(expected, newCount)
          case Stop =>
            Behaviors.stopped
          case _ => Behaviors.unhandled
        }

      idle
    }


object BoidActor:
  sealed trait Command
  case class DoUpdate(replyTo: ActorRef[World.Operations], id: Int, model: BoidsModel) extends Command

  def apply(i: Int): Behavior[Command] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Actor $i created")
      Behaviors.receiveMessage {
        case DoUpdate(replyTo, id, model) =>
          getMyBoids(i, model).foreach(_.updateVelocity(model))
          getMyBoids(i, model).foreach(_.updatePos(model))
          replyTo ! World.Operations.Ack(id)
          Behaviors.same
      }
    }

  def getMyBoids(i: Int, model: BoidsModel): List[pcd.Boid] =
    val allBoids = model.getBoids
    val start = i * 50
    val end = Math.min(start + 50, allBoids.size)
    if start >= allBoids.size then
      List()
    else
      allBoids.slice(start, end)
