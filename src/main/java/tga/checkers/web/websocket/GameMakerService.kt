package tga.checkers.web.websocket

import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller
import java.util.concurrent.atomic.AtomicInteger

@Controller
class GameMakerService {

    private val counter = AtomicInteger(0)


    @SubscribeMapping("/user/queue/new-game-request")
    fun gameMakeRequest(): FoundGameDescriptor? {
        return FoundGameDescriptor(
            gameId = counter.incrementAndGet(),
            color = "black",
            partnerName = "Server-Bot"
        )
    }

}

data class FoundGameDescriptor(
    val gameId: Int,
    val color: String,
    val partnerName: String
)
