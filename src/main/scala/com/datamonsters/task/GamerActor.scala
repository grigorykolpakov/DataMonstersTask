package com.datamonsters.task

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp
import akka.io.Tcp.{PeerClosed, Received}

/**
  * Created by gri on 10/15/16.
  */


object GamerActor {
  //  var count: Int = 0;
  val msgStart = "Привет! Попробую найти тебе противника."
  val msgStartStep = "Противник найден. Нажмите пробел, когда увидите цифру 3"
  val msgPospeshil = "Вы поспешили и проиграли"
  val msgYouLooser = "Вы  проиграли"
  val msgYouWinYourCompetitorErr = "Ваш противник поспешил и вы выйграли"
  val msgStepWin = "Вы нажали пробел первым и победили"
}

class GamerActor(val svrForTelnet: ActorRef) extends Actor {
  var gameActor: ActorRef = null
  var timeReceive3: Long = 0

  import Tcp._

  def receive = {
    case mTheEnd: MsgGameOver => {
      svrForTelnet ! new MsgGameOver
      context.stop(self)
    }

    case mmm: MsgYouStartBeforeGame => {
      svrForTelnet ! new MsgStringForTelnet(GamerActor.msgPospeshil)
    }

    case mm: MsgYouWinYourCompetitorErr => {
      svrForTelnet ! MsgStringForTelnet(GamerActor.msgYouWinYourCompetitorErr)
      self ! new MsgGameOver
    }

    case mMsgYouWin: MsgYouWin => {
      svrForTelnet ! MsgStringForTelnet(GamerActor.msgStepWin)
      self ! new MsgGameOver
    }

    case mMsgYouLooser: MsgYouLooser => {
      svrForTelnet ! MsgStringForTelnet(GamerActor.msgYouLooser)
      self ! new MsgGameOver
    }

    case m: MsgErrFromGamer => {
      svrForTelnet ! MsgStringForTelnet(GamerActor.msgYouWinYourCompetitorErr)
    }

    case mStep: MsgGameStep => {
      svrForTelnet ! MsgStringForTelnet(" " + mStep.num + "\n")
      if (mStep.num == 3) {
        timeReceive3 = System.currentTimeMillis()
      }
    }

    case mm: MsgGameFirstStep => {
      svrForTelnet ! MsgStringForTelnet(GamerActor.msgStartStep)
    }

    case mm: MsgGameRegistration => {
      gameActor = mm.gameActor
    }

    case m: MsgGamerStart => {
      svrForTelnet ! new MsgGamerReadyForGame
      svrForTelnet ! MsgStringForTelnet(GamerActor.msgStart)
    }

    case r@Received(data) => {
      if (gameActor != null && data.utf8String.equalsIgnoreCase(" \r\n")) {
        val timeReceiveSpace = System.currentTimeMillis()
        gameActor ! new MsgSpaceFromGamer(timeReceiveSpace)
      }
    }

    case PeerClosed => {
      println(" case PeerClosed =>")
      context stop self
    }

  }

}

