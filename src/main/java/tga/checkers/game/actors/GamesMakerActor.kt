package tga.checkers.game.actors

import akka.actor.AbstractLoggingActor
import akka.japi.pf.ReceiveBuilder
import org.springframework.messaging.simp.SimpMessagingTemplate
import tga.checkers.exts.linkedListOf
import tga.checkers.exts.on
import tga.checkers.exts.actorOf
import tga.checkers.game.model.GameRequest
import tga.checkers.game.model.Player
import java.util.*

//todo: this actor should be a standalone on a cluster!!!
class GamesMakerActor(
        val websocket: SimpMessagingTemplate
) : AbstractLoggingActor() {

    val gameRequestsQueue: Queue<GameRequest> = linkedListOf()  // todo: make it work on cluster and after reboot
    var gamesCounter: Int = 0 // todo: make it work on cluster and after reboot

    override fun createReceive(): Receive = ReceiveBuilder()
            .on(GameRequest::class, this::onGameRequest)
            .build()

    private fun onGameRequest(gameRequest: GameRequest) {
        gameRequestsQueue += gameRequest
        tryToStartNewGame()
    }

    private fun tryToStartNewGame() {
        log().debug("tryToStartNewGame")
        val players: Collection<Player>? = findPlayersForNewGame()
        if (players != null) {
            startNewGame(players)
        }
    }

    private fun startNewGame(players: Collection<Player>) {
        log().debug("startNewGame(players={})", players)
        val gameId = (++gamesCounter)
        context.actorOf("game-$gameId"){ GameActor(gameId, players, websocket) }
    }


    private fun findPlayersForNewGame(): Collection<Player>? {
        log().debug("findPlayersForNewGame")
        //todo: Make a real implementation
        if (gameRequestsQueue.size < 2) return null

        val player1 = gameRequestsQueue.remove().player.copy(gameRole = "black")
        val player2 = gameRequestsQueue.remove().player.copy(gameRole = "white")

        return listOf( player1, player2 )
    }
}
