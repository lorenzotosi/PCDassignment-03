package ass3p1

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import ass3p1.World.Operations.*

object Guardian:
  enum Command:
    case StartWorld(nBoids: Int)
    case RestartWorld(nBoids: Int)
    case StopWorld

  import Command.*

  def apply(): Behavior[Command] =
    Behaviors.setup { ctx =>
      var currentWorld: Option[ActorRef[World.Operations]] = None

      Behaviors.receiveMessage {
        case StartWorld(n) =>
          ctx.log.info(s"Guardian starting World with $n boids")
          val world = ctx.spawn(World(n), "World")
          currentWorld = Some(world)
          Behaviors.same

        case RestartWorld(n) =>
          ctx.log.info(s"Guardian restarting World with $n boids")
          currentWorld.foreach { w =>
            ctx.stop(w) // termina il world attuale
          }
          val newWorld = ctx.spawn(World(n), "WorldNew_" + System.nanoTime())
          currentWorld = Some(newWorld)
          Behaviors.same

        case StopWorld =>
          ctx.log.info("Guardian stopping World")
          currentWorld.foreach(ctx.stop)
          currentWorld = None
          Behaviors.same
      }
    }


object World:
  enum Operations:
    case Start
    case Stop
    case Update
    case Ack(from: Int)

  export Operations.*

  def apply(nBoids: Int): Behavior[Operations] =
    Behaviors.setup { ctx =>
      val group = if nBoids % 50 != 0 then nBoids / 50 + 1 else nBoids / 50

      val boidActors: Seq[ActorRef[Dummy.Command]] =
        for i <- 0 until group yield
          ctx.spawn(Dummy(i), s"dummy${i}_${System.nanoTime()}")

      def idle: Behavior[Operations] =
        Behaviors.receiveMessage {
          case Start =>
            ctx.log.info(s"World($nBoids) started")
            Behaviors.same
          case Stop =>
            ctx.log.info(s"World($nBoids) stopped")
            Behaviors.stopped
          case Update =>
            ctx.log.info(s"World($nBoids) updating...")
            boidActors.zipWithIndex.foreach { case (actor, id) =>
              actor ! Dummy.DoUpdate(ctx.self, id)
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
            ctx.log.info(s"World($nBoids) received ACK $newCount/$expected from Dummy $from")
            if newCount == expected then
              ctx.log.info(s"World($nBoids) update completed ✅")
              idle
            else
              waitingAcks(expected, newCount)
          case Stop =>
            ctx.log.info(s"World($nBoids) stopped while waiting ACKs")
            Behaviors.stopped
          case _ => Behaviors.unhandled
        }

      idle
    }


object Dummy:
  sealed trait Command
  case class DoUpdate(replyTo: ActorRef[World.Operations], id: Int) extends Command

  def apply(i: Int): Behavior[Command] =
    Behaviors.setup { ctx =>
      ctx.log.info(s"Dummy $i created")
      Behaviors.receiveMessage {
        case DoUpdate(replyTo, id) =>
          ctx.log.info(s"Dummy $i received update")
          replyTo ! World.Operations.Ack(id)
          Behaviors.same
      }
    }


@main def worldExample(): Unit =
  val system = ActorSystem(Guardian(), "GuardianSystem")

  import Guardian.Command.*

  system ! StartWorld(1500)
  Thread.sleep(2000)
  system ! RestartWorld(300)
  Thread.sleep(2000)
  system ! StopWorld
