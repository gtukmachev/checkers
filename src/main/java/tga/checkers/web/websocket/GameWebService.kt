package tga.checkers.web.websocket

import akka.actor.ActorNotFound
import akka.actor.ActorRef
import akka.actor.ActorSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller
import tga.checkers.exts.sec
import tga.checkers.game.actors.GameRequest
import tga.checkers.game.actors.PlayerStep
import tga.checkers.game.actors.StatusRequest
import java.security.Principal
import java.util.concurrent.TimeUnit

@Controller
class GameWebService(
        private val akka: ActorSystem,
        private val gameMakerActor: ActorRef
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(GameWebService::class.java);
    }

    /**
     * __/user/queue/game__ - the main game channel for players
     * On SUBSCRIPTION : user will receive a GameStatus message
     */
    @SubscribeMapping("/user/queue/game")
    fun connectToGameChannel(principal: Principal) {
        gameMakerActor.tell( StatusRequest(principal.name), ActorRef.noSender() )
    }


    /**
     * Request to start a new game.
     * Response will be sent to "/user/queue/game" channel to all players (after the game will be created)
     */
    @MessageMapping("/queue/new-game")
    fun gameMakeRequest(principal: Principal) {
        val msg = GameRequest( principal.name )
        gameMakerActor.tell(msg, ActorRef.noSender())
    }


    /**
     * A message from a player about his step in his game
     */
    @MessageMapping("/queue/steps")
    fun stepFromPlayer(
                     principal: Principal,
            @Payload      step: PlayerStep
    ) {
        log.debug("fromPlayer(principal={})", principal.name, step)
        val playerActor = findPlayerActor(principal.name)
        playerActor?.tell(step, ActorRef.noSender())
    }


    private fun findPlayerActor(playerName: String): ActorRef? {
        val actorName = "/user/game-maker/player-$playerName"
        return try {
            akka
                .actorSelection(actorName)
                .resolveOne(1.sec())
                .toCompletableFuture()
                .get(1, TimeUnit.SECONDS)

        } catch (t: ActorNotFound) {
            log.error("""
                The actor not found: '$actorName'.
                Check application configuration of the WebSocket security layer:
                    the message should be rejected on the security layer, 
                    and do no achieve the controller!
            """.trimIndent(), t)
            null
        }
    }
}

