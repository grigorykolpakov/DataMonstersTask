package com.datamonsters.task

/**
  * Created by gri on 10/15/16.
  */


import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef}

import scala.concurrent.duration.FiniteDuration


object GameActor {

}

class GameActor(val gamer1: ActorRef, val gamer2: ActorRef) extends Actor {
  var gamer1Time: Long = -1
  var gamer2Time: Long = -1
  var gameSpaceTime: Long = -1

  def receive = {
    case mErr: MsgErrFromGamer => {
      if (sender() == gamer1) {
        gamer2 ! mErr
        gamer1 ! MsgYouWin

      } else {
        gamer1 ! mErr
        gamer2 ! MsgYouWin
      }
      self ! MsgGameOver
    }

    case m: MsgGameStart => {
      gamer1 ! new MsgGameRegistration(self)
      gamer2 ! new MsgGameRegistration(self)
      val system = context.system
      import system.dispatcher
      system.scheduler.scheduleOnce(FiniteDuration(3, TimeUnit.SECONDS), self, new MsgGameFirstStep)
    }


    case mm: MsgGameFirstStep => {
      gamer1 ! mm
      gamer2 ! mm
      sendGameStep
    }

    case mm: MsgGameStep => {
      gamer1 ! mm
      gamer2 ! mm
      sendGameStep
    }

    case mTheEnd: MsgGameOver => {
      bGameInAction = false
      gamer1 ! MsgGameOver
      gamer2 ! MsgGameOver
      context.stop(self)
    }

    case m: MsgSpaceFromGamer => {
      if (sender() == gamer1) {
        gamer1Time = m.time
        if ((gameSpaceTime < 0) || (gameSpaceTime > gamer1Time)) {
          gamer1 ! new MsgYouStartBeforeGame
          gamer2 ! new MsgYouWinYourCompetitorErr
        } else {
          gamer1 ! new MsgYouWin
          gamer2 ! new MsgYouLooser
        }
      } else {
        gamer2Time = m.time
        if ((gameSpaceTime < 0) || (gameSpaceTime > gamer2Time)) {
          gamer2 ! new MsgYouStartBeforeGame
          gamer1 ! new MsgYouWinYourCompetitorErr
        } else {
          gamer2 ! new MsgYouWin
          gamer1 ! new MsgYouLooser
        }
      }
      self ! new MsgGameOver
    }
  }


  var bGameInAction = true

  val r = scala.util.Random

  def getNextGameNum(): Int = {
    r.nextInt(3) + 1
  }


  def getNextGameTimeInterval(): Int = {
    r.nextInt(3) + 2
  }


  def sendGameStep(): Unit = {
    if (bGameInAction) {
      val system = context.system

      import system.dispatcher

      val stepNum = getNextGameNum()
      if (stepNum == 3) {
        gameSpaceTime = System.currentTimeMillis()
      }
      system.scheduler.scheduleOnce(FiniteDuration(getNextGameTimeInterval(), TimeUnit.SECONDS), self,
        new MsgGameStep(stepNum))

    }
  }
}

