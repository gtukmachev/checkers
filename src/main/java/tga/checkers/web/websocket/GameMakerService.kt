package tga.checkers.web.websocket

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Controller
import java.security.Principal
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

@Controller
class GameMakerService(
        private val simpMessagingTemplate: SimpMessagingTemplate
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(GameMakerService::class.java);
    }

    private val counter = AtomicInteger(0)

    private val waitUser = ConcurrentLinkedQueue<Principal>()


    @SubscribeMapping("/user/queue/new-game-request")
    fun gameMakeRequest(principal: Principal) {
        waitUser.add(principal)
        tryToStartGame();
    }

    private fun tryToStartGame() {
        log.trace("tryToStartGame(): waitUser.size = {}", waitUser.size)
        if (waitUser.size < 2) return

        val user1: Principal = waitUser.remove()
        val user2: Principal = waitUser.remove()

        val gameId = counter.incrementAndGet();

        sendNewUserToPlayer(user1, gameId, "white", user2)
        sendNewUserToPlayer(user2, gameId, "black", user1)
    }

    private fun sendNewUserToPlayer(player: Principal, gameId: Int, color: String, opponent: Principal) {
        val foundGameDescriptor = FoundGameDescriptor(gameId, color, opponent.name)
        val username = player.name
        simpMessagingTemplate.convertAndSendToUser(username, "/queue/new-game-request", foundGameDescriptor)
    }

}

data class FoundGameDescriptor(
    val gameId: Int,
    val color: String,
    val partnerName: String
)
