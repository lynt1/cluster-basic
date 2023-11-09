
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

trait SenderCommand
case class ListingSFSActor(listing: Receptionist.Listing) extends SenderCommand with ReceiverCommand

object Sender {

  def apply: Behavior[SenderCommand] = Behaviors.setup{ ctx =>
    ctx.spawn(ClusterListener(), "ClusterListener")
    val receptionistAdapter = ctx.messageAdapter[Receptionist.Listing](ListingSFSActor)
    ctx.system.receptionist ! Receptionist.subscribe(Key.receiver, receptionistAdapter)
    ctx.system.receptionist ! Receptionist.register(Key.sender, ctx.self)
    Behaviors.receiveMessagePartial {
      case ListingSFSActor(listing) =>
        val address = listing.serviceInstances(Key.receiver)
        if(address.isEmpty) {
          ctx.log.info("address is empty")
          Behaviors.same
        } else {
          ctx.log.info("Sender send message to receiver")
          address.head ! Message("Hello Receiver")
          new Sender(ctx, address.head).ready
        }
    }
  }
}

class Sender(context: ActorContext[SenderCommand], receiver: ActorRef[ReceiverCommand]) {
  def ready: Behavior[SenderCommand] = Behaviors.receiveMessagePartial{
    case Message(text) =>
      context.log.info(s"Sender on ready, message: $text")
      Behaviors.same
  }
}

