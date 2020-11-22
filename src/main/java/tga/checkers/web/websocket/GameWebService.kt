package tga.checkers.web.websocket

import akka.actor.ActorNotFound
import akka.actor.ActorRef
import akka.actor.ActorSystem
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller
import tga.checkers.exts.sec
import tga.checkers.game.actors.GameRequest
import tga.checkers.game.actors.PlayerStep
import tga.checkers.game.model.Player
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

    @SubscribeMapping("/user/queue/new-game-request")
    fun gameMakeRequest(principal: Principal) {
        // todo: pass a real user Id (from DB)
        // todo: think if it a good idea to pass Principal to Akka level?
        val msg = GameRequest( Player(userId = 0, name = principal.name, gameRole = "undefined") )
        gameMakerActor.tell(msg, ActorRef.noSender())
    }

    @MessageMapping("/queue/game/{gameId}/step")
    fun fromPlayer(
                                           principal  : Principal,
            @DestinationVariable("gameId") gameId     : Int,
            @Payload                       playerStep : PlayerStep
    ) {
        log.debug("fromPlayer(gameId={})", gameId)
        val playerActor = findPlayerActor(gameId, principal.name)
        playerActor?.tell(playerStep, ActorRef.noSender())
    }


    fun findPlayerActor(gameId: Int, playerName: String): ActorRef? {
        val actorName = "/user/game-maker/game-$gameId/player-$playerName"
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

