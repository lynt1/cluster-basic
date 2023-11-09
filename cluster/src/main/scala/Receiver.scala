

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}

trait ReceiverCommand

case class Message(text: String) extends ReceiverCommand with SenderCommand
object Receiver {

  def apply: Behavior[ReceiverCommand] = Behaviors.setup{ ctx =>
    ctx.spawn(ClusterListener(), "ClusterListener")
    ctx.system.receptionist ! Receptionist.register(Key.receiver, ctx.self)
    val receptionistAdapter = ctx.messageAdapter[Receptionist.Listing](ListingSFSActor)
    ctx.system.receptionist ! Receptionist.subscribe(Key.sender, receptionistAdapter)
    Behaviors.receiveMessagePartial {
      case ListingSFSActor(listing) =>
        val address = listing.serviceInstances(Key.sender)
        if (address.isEmpty) {
          ctx.log.info("address is empty")
          Behaviors.same
        } else {
          new Receiver(ctx, address.head).ready
        }
    }

  }
}

class Receiver(ctx: ActorContext[ReceiverCommand], sender: ActorRef[SenderCommand]){
  def ready: Behavior[ReceiverCommand] = {
    Behaviors.receiveMessagePartial{
      case Message(text) =>
        ctx.log.info(s"Receiver on ready, message: $text")
        ctx.log.info(s"Receiver rep message to sender, message: Hello Sender")
        sender ! Message("Hello Sender")
        Behaviors.same
      case _ =>
        ctx.log.info("other message")
        Behaviors.same
    }
  }
}

object Key {
  val receiver = ServiceKey[ReceiverCommand]("receiver-actor")
  val sender = ServiceKey[SenderCommand]("sender-actor")
}

