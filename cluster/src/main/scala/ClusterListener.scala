

import Other.OtherCommand
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent
import akka.cluster.ClusterEvent._
import akka.cluster.typed.{Cluster, Join, Subscribe}
import com.typesafe.config.ConfigFactory
object ClusterListener {

  sealed trait Command
  // internal adapted cluster events only
  private final case class ReachabilityChange(reachabilityEvent: ReachabilityEvent) extends Command
  private final case class MemberChange(event: MemberEvent) extends Command

  def apply(): Behavior[Command] = Behaviors.setup { ctx =>
    val memberEventAdapter: ActorRef[MemberEvent] = ctx.messageAdapter(MemberChange)
    val cluster = Cluster(ctx.system)
    cluster.subscriptions ! Subscribe(memberEventAdapter, classOf[MemberEvent])

    val reachabilityAdapter = ctx.messageAdapter(ReachabilityChange)
    cluster.subscriptions ! Subscribe(reachabilityAdapter, classOf[ReachabilityEvent])

    Behaviors.receiveMessage { message =>
      message match {
        case ReachabilityChange(reachabilityEvent) =>
          reachabilityEvent match {
            case ClusterEvent.UnreachableMember(member) =>
              ctx.log.info("Member detected as unreachable: {}", member)
            case ClusterEvent.ReachableMember(member) =>
              ctx.log.info("Member back to reachable: {}", member)
            case _ =>
              ctx.log.info("Other cluster event")
          }
          Behaviors.same

        case MemberChange(changeEvent) =>
          changeEvent match {
            case MemberJoined(member) =>
              ctx.log.info(s"Member joined, address: ${member.address}")

            case MemberUp(member) =>
              ctx.log.info("Member Up: {}", member.address)

            case MemberRemoved(member, previousStatus) =>
              ctx.log.info("Member Removed: {} after {}", member.address, previousStatus)

            case MemberWeaklyUp(member) =>
              ctx.log.info(s"Member ${member.address} Weakly Up")

            case MemberLeft(member) =>
              ctx.log.info(s"Member ${member.address} left")

            case MemberExited(member) =>
              ctx.log.info(s"Member ${member.address} existed")

            case MemberDowned(member) =>
              ctx.log.info(s"Member ${member.address} down")

            case _: MemberEvent => // ignore
              ctx.log.info("Other member event")
          }

        case _ =>
          println("other")
      }
      Behaviors.same
    }
  }
}

object Main extends App{

  object RootBehavior {
    def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
      context.spawn(ClusterListener(), "ClusterListener")
      Behaviors.empty
    }
  }

  val portSender = 2551
  val portReceiver = 2553
  val portOther = 2552
  val configs = List(portSender, portOther, portReceiver).map{port =>
    ConfigFactory.parseString(
      s"""
    akka.remote.artery.canonical.port=$port
    """).withFallback(ConfigFactory.load("application.conf"))
  }
/*  val configSender = ConfigFactory.parseString(
    s"""
  akka.remote.artery.canonical.port=$portSender
  """).withFallback(ConfigFactory.load("application.conf"))

  val configReceiver = ConfigFactory.parseString(
    s"""
      akka.remote.artery.canonical.port=$portReceiver
      """).withFallback(ConfigFactory.load("application.conf"))*/

  ActorSystem[SenderCommand](Sender.apply, "ClusterSystem", configs.head)
  ActorSystem[ReceiverCommand](Receiver.apply, "ClusterSystem", configs.reverse.head)
  ActorSystem[OtherCommand](Other.apply, "ClusterSystem", configs.drop(1).head)
}
