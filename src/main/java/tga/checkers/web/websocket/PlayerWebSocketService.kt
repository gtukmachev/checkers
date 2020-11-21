package tga.checkers.web.websocket

import akka.actor.ActorRef
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller
import tga.checkers.game.actors.GameRequest
import tga.checkers.game.model.Player
import java.security.Principal

@Controller
class GameMakerService(
        private val simpMessagingTemplate: SimpMessagingTemplate,
        private val gameMakerActor: ActorRef
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(GameMakerService::class.java);
    }

    @SubscribeMapping("/user/queue/new-game-request")
    fun gameMakeRequest(principal: Principal) {
        // todo: pass a real user Id (from DB)
        // todo: think if it a good idea to pass Principal to Akka level?
        val msg = GameRequest( Player(userId = 0, name = principal.name, gameRole = "undefined") )
        gameMakerActor.tell(msg, ActorRef.noSender())
    }

}

