import Other.OtherCommand
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.ConfigFactory

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
  ActorSystem[SenderCommand](Sender.apply, "ClusterSystem", configs.head)
  ActorSystem[ReceiverCommand](Receiver.apply, "ClusterSystem", configs.reverse.head)
  ActorSystem[OtherCommand](Other.apply, "ClusterSystem", configs.drop(1).head)
}
