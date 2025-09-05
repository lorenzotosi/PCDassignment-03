package ass3p1

import akka.actor.Actor
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import ass3p1.pcd.Boid

object Actor:
  
  enum Command:
    case Create
    
  export Command.*
  
  def apply(myBoids: List[Boid]): Behavior[Command] = Behaviors.receive: (context, msg) =>
    msg match
      //case Create => new Boid(p1, v1)
      case _ => Behaviors.stopped

  
