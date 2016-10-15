package com.datamonsters.task

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString

import scala.collection.mutable.ListBuffer

/**
  * Created by gri on 10/15/16.
  */
object TaskApp extends App {

  val system = ActorSystem("gri-service-system")
  //  val endpoint = new InetSocketAddress("localhost", 11111)
  //  system.actorOf(SvrForTelnet.props(endpoint), "echo-service")
  system.actorOf(Props[SvrForTelnet], "echo-service")

  readLine(s"Hit ENTER to exit ...${System.getProperty("line.separator")}")
  system.shutdown()
}



class SvrForTelnet extends Actor {
  val listGamers = new ListBuffer[TelnetGamer]()

  import Tcp._
  import context.system

  //IO(Tcp) ! Bind(self, new InetSocketAddress("192.168.1.34", 11111))
  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 11111))

  def getTelnetGamerByActor(actor: ActorRef): Option[TelnetGamer] = {
    listGamers.find(_.gamerActor == actor)
  }

  def getGamersPair(actorRef: ActorRef): Option[(TelnetGamer, TelnetGamer)] = {
    getTelnetGamerByActor(actorRef) match {
      case Some(x: TelnetGamer) => {
        val secondOpt = listGamers.find(p => (p.inGame == false) && (p.gamerActor != actorRef))
        secondOpt match {
          case Some(xs: TelnetGamer) => {
            Some((x, xs))
          }
          case None => None
        }
      }
      case None => None
    }

  }

  def receive = {
    case mTheEnd: MsgGameOver => {
      val gamerActor = sender()
      getTelnetGamerByActor(gamerActor) match {
        case Some(x: TelnetGamer) => {
          x.inGame = false
          x.gamerActor ! PoisonPill
          listGamers -= x
        }
        case None => {

        }
      }

    }
    case b@Bound(localAddress) => {
      println("localAddress=" + localAddress)
      println("b=" + b)
    }

    case mm: MsgGamerReadyForGame => {
      print("Svr mm: MsgGamerReadyForGame ")
      val act = sender()
      getGamersPair(act) match {
        case Some(pairGamers) => {
          print("find 2 gamers !!!! ")
          pairGamers._1.inGame = true
          pairGamers._2.inGame = true
          val actorGame = context.actorOf(Props(
            new GameActor(pairGamers._1.gamerActor, pairGamers._2.gamerActor)))
          //    new GameActor(pairGamers._1.gamerActor, pairGamers._1.gamerActor)), "game" + System.currentTimeMillis())
          println("new game Actor = " + actorGame)
          actorGame ! new MsgGameStart
        }
        case None => {
          print("not 2 gamers ")
        }
      }

    }

    case CommandFailed(_: Bind) => context stop self

    case c@Connected(remote, local) => {
      println("remote=" + remote)
      println("local=" + local)
      println("c=" + c)
      val gamerActor = context.actorOf(
        Props(new GamerActor(self)))
      val connection = sender()
      connection ! Register(gamerActor, true, true)
      listGamers += new TelnetGamer(gamerActor, connection)
      println( "listGamers.size="+ listGamers.size)
      gamerActor ! new MsgGamerStart

    }

    case m: MsgStringForTelnet => {
      getTelnetGamerByActor(sender()) match {
        case Some(x: TelnetGamer) => {
          x.connectActor ! Write(ByteString(m.sMsg + "\t\n"))
        }

        case None => {

        }
      }

    }

  }


}
