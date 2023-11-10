import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object Other {

  trait OtherCommand

  def apply: Behavior[OtherCommand] = Behaviors.setup{context =>
    context.spawn(ClusterListener(), "ClusterListener")
    Behaviors.same
  }

}
