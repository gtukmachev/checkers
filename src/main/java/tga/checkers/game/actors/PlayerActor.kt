package tga.checkers.game.actors

import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import akka.japi.pf.ReceiveBuilder
import org.springframework.messaging.simp.SimpMessagingTemplate
import tga.checkers.exts.on


interface PlayerActorMessage
    data class JoinToGame(val gameActor: ActorRef): PlayerActorMessage


class PlayerActor(
        private val gameId: Int,
        private val playerName: String,
        private val playerIndex: Int,
        private val websocket: SimpMessagingTemplate
) : AbstractLoggingActor()  {

    private lateinit var gameActor: ActorRef

    override fun createReceive(): Receive = ReceiveBuilder()
            .on(              JoinToGame::class){ onJoinToGame(it)       }
            .on(         ToPlayerMessage::class){ tellToUser(it)         }
            .on( WebServiceIncomeMessage::class){ tellToGame(it)         }
            .build()

    private fun onJoinToGame(joinToGameMsg: JoinToGame) {
        gameActor = joinToGameMsg.gameActor
    }

    private fun tellToUser(msg: ToPlayerMessage) {
        log().debug("tellToUser(msg={})",  msg)
        val webSocketMessage = WebServiceOutcomeMessage(gameId, msg.javaClass.simpleName, msg)
        sendToUser(webSocketMessage, "/queue/game")
    }

    private fun tellToGame(msg: WebServiceIncomeMessage) {
        log().debug("tellToGame(msg={})",  msg)
        gameActor.tell(msg, self)
    }

    private fun sendToUser(msg: Any, queue: String) {
        log().debug("sendToUser(queue='{}', msg={})", queue, msg)
        websocket.convertAndSendToUser(playerName, queue, msg)
    }

}
