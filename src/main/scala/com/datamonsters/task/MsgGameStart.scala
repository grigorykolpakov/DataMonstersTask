package com.datamonsters.task

import akka.actor.ActorRef

/**
  * Created by gri on 10/15/16.
  */

case class MsgGamerStart()

case class MsgGameRegistration(gameActor: ActorRef)

case class MsgGameStart()

case class MsgGamerReadyForGame()

case class MsgSpaceFromGamer(time: Long)

case class MsgStringForTelnet(sMsg: String)

case class MsgErrFromGamer()

case class MsgYouWin()

case class MsgYouLooser()

case class MsgYouStartBeforeGame()

case class MsgYouWinYourCompetitorErr()

case class MsgGameOver()

case class MsgGameStep(num: Int)

case class MsgGameFirstStep()
