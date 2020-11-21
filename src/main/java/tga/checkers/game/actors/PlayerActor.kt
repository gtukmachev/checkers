package tga.checkers.game.actors

import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import akka.japi.pf.ReceiveBuilder
import org.springframework.messaging.simp.SimpMessagingTemplate
import tga.checkers.exts.on
import tga.checkers.game.model.Player


interface PlayerActorMessage
    class   GameHasBeenStarted : PlayerActorMessage
    class             YourStep : PlayerActorMessage
    class ItIsNotYourStepError : PlayerActorMessage
    class            GameState : PlayerActorMessage

interface WebServiceOutcomeMessage
    data class GameDescriptor(val gameId: Int, val color: String                ): WebServiceOutcomeMessage
    data class    GameMessage(val gameId: Int, val msgType: String, val msg: Any): WebServiceOutcomeMessage

interface WebServiceIncomeMessage


class PlayerActor(
        private val gameId: Int,
        private val player: Player,
        private val gameActor: ActorRef,
        private val websocket: SimpMessagingTemplate
) : AbstractLoggingActor()  {

    override fun createReceive(): Receive = ReceiveBuilder()
            .on(  GameHasBeenStarted::class){ onGameHasBeenStarted()     }
            .on(            YourStep::class){ onYourStep(it)             }
            .on(ItIsNotYourStepError::class){ onItIsNotYourStepError(it) }
            .on(           GameState::class){ onGameState(it)            }
            .build()

    private fun onGameHasBeenStarted() {
        log().debug("onGameHasBeenStarted")
        sendToUser(GameDescriptor(gameId, player.gameRole), "/queue/new-game-request")
    }

    private fun onYourStep(yourStepMessage: YourStep) {
        log().debug("onYourStep")
        tellUser( yourStepMessage )
    }

    private fun onItIsNotYourStepError(itIsNotYourStepError: ItIsNotYourStepError) {
        log().debug("onItIsNotYourStepError(msg={})", itIsNotYourStepError)
        tellUser( itIsNotYourStepError )
        //TODO("Not yet implemented")
    }

    private fun onGameState(gameState: GameState) {
        log().debug("onGameState(msg={})", gameState)
        tellUser( gameState )
        TODO("Not yet implemented")
    }

    private fun tellUser(msg: Any) {
        log().debug("tellUser(msg={})",  msg)
        val webSocketMessage = GameMessage(gameId, msg.javaClass.simpleName, msg)
        sendToUser(webSocketMessage, "/queue/game/$gameId")
    }

    private fun sendToUser(msg: Any, queue: String) {
        log().debug("sendToUser(queue='{}', msg={})", queue, msg)
        websocket.convertAndSendToUser(player.name, queue, msg)
    }
}
