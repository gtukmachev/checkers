package tga.checkers.game.actors

import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import akka.japi.pf.ReceiveBuilder
import org.springframework.messaging.simp.SimpMessagingTemplate
import tga.checkers.exts.on
import tga.checkers.game.model.Player


interface PlayerActorMessage
    class   GameHasBeenStarted : PlayerActorMessage

interface ToPlayerMessage
    class             YourStep(val nTurn: Int) : ToPlayerMessage
    class ItIsNotYourStepError                 : ToPlayerMessage
    class            GameState                 : ToPlayerMessage

interface WebServiceOutcomeMessage
    data class GameDescriptor(val gameId: Int, val color: String                ): WebServiceOutcomeMessage
    data class    GameMessage(val gameId: Int, val msgType: String, val msg: Any): WebServiceOutcomeMessage

interface WebServiceIncomeMessage
    data class PlayerStep(val lin: Int, val col: Int) : WebServiceIncomeMessage


class PlayerActor(
        private val gameId: Int,
        private val player: Player,
        private val gameActor: ActorRef,
        private val websocket: SimpMessagingTemplate
) : AbstractLoggingActor()  {

    override fun createReceive(): Receive = ReceiveBuilder()
            .on(      GameHasBeenStarted::class){ onGameHasBeenStarted() }
            .on(         ToPlayerMessage::class){ tellToUser(it)           }
            .on( WebServiceIncomeMessage::class){ tellToGame(it)           }
            .build()

    private fun onGameHasBeenStarted() {
        log().debug("onGameHasBeenStarted")
        sendToUser(GameDescriptor(gameId, player.gameRole), "/queue/new-game-request")
    }

    private fun tellToUser(msg: Any) {
        log().debug("tellToUser(msg={})",  msg)
        val webSocketMessage = GameMessage(gameId, msg.javaClass.simpleName, msg)
        sendToUser(webSocketMessage, "/queue/game/$gameId")
    }

    private fun tellToGame(msg: WebServiceIncomeMessage) {
        log().debug("tellToGame(msg={})",  msg)
        gameActor.tell(msg, self)
    }

    private fun sendToUser(msg: Any, queue: String) {
        log().debug("sendToUser(queue='{}', msg={})", queue, msg)
        websocket.convertAndSendToUser(player.name, queue, msg)
    }

}
