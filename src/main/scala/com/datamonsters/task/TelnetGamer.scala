package com.datamonsters.task

import akka.actor.ActorRef

/**
  * Created by gri on 10/15/16.
  */
class TelnetGamer(val gamerActor: ActorRef,val connectActor: ActorRef) {
  var inGame: Boolean = false
}
