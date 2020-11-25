package tga.checkers.game.actors

import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import akka.actor.Terminated
import akka.japi.pf.ReceiveBuilder
import org.springframework.messaging.simp.SimpMessagingTemplate
import tga.checkers.exts.*
import java.util.*

interface GamesMakerActorMessages
    data class   GameRequest(val userName: String) : GamesMakerActorMessages
    data class StatusRequest(val userName: String) : GamesMakerActorMessages

interface PlayerIndexItem
        object WaitingIndexItem : PlayerIndexItem
    data class InGameIndexItem(val gameActor: ActorRef) : PlayerIndexItem


//todo: this actor should be a standalone on a cluster!!!
class GamesMakerActor(
        val websocket: SimpMessagingTemplate
) : AbstractLoggingActor() {

    val gameRequestsQueue: Queue<GameRequest> = linkedListOf()  // todo: make it work on cluster and after reboot
    val playersToGameIndex: MutableMap<String, PlayerIndexItem> = HashMap()

    var gamesCounter: Int = 0 // todo: make it work on cluster and after reboot

    override fun createReceive(): Receive = ReceiveBuilder()
            .on(  GameRequest::class, ::onGameRequest  )
            .on(StatusRequest::class, ::onStatusRequest)
            .on(   Terminated::class, ::onTerminated   )
            .build()

    private fun onTerminated(terminatedMsg: Terminated) {
        log().debug("onTerminated(terminatedMsg={})", terminatedMsg)
        val playerPart =
                terminatedMsg.actor.path().elements.find{ it.startsWith("player-") }
                ?: return

        val playerName = playerPart.substring("player-".length)
        val removedItem = playersToGameIndex.remove(playerName)
        log().debug("onTerminated(terminatedMsg={}) :: playersToGameIndex.remove({}) => {}", terminatedMsg, playerName, removedItem)
    }

    private fun onStatusRequest(statusRequest: StatusRequest) {
        log().debug("onStatusRequest(statusRequest={})", statusRequest)
        notifyUserOrPerform(statusRequest.userName){ }
    }

    private fun onGameRequest(gameRequest: GameRequest) {
        log().debug("onGameRequest(gameRequest={})", gameRequest)
        notifyUserOrPerform(gameRequest.userName){
            gameRequestsQueue += gameRequest
            playersToGameIndex[gameRequest.userName] = WaitingIndexItem
            tryToStartNewGame()
        }
    }

    private fun notifyUserOrPerform(userName: String, ifNoUserFoundCallback: () -> Unit) {
        log().debug("notifyUserOrPerform(userName={}, ifNoUserFoundCallback={})", userName, ifNoUserFoundCallback)
        when(val indexStatus = playersToGameIndex[userName]) {
                          null -> ifNoUserFoundCallback()
              WaitingIndexItem -> respondWithWaitingStatus(userName)
            is InGameIndexItem -> indexStatus.gameActor.tell(NotifyPlayer(userName), self)
        }
    }

    private fun respondWithWaitingStatus(userName: String) {
        log().debug("respondWithWaitingStatus(userName={})", userName)
        val msg = WaitingForAGame
        val gameMessage = GameMessage(-1, msg.javaClass.simpleName, msg)
        websocket.convertAndSendToUser(userName, "/queue/game", gameMessage)
    }

    private fun tryToStartNewGame() {
        log().debug("tryToStartNewGame")
        findPlayersForNewGame()?.let( ::startNewGame )
    }

    private fun startNewGame(playerNames: Collection<String>) {
        log().debug("startNewGame(playerNames={})", playerNames)
        val gameId = (++gamesCounter)

        val playerActorsCollection: Collection<Player> = playerNames.mapIndexed{ i, playerName ->
            val playerNum = i + 1
            val playerActor = context.actorOf("player-$playerName"){ PlayerActor(gameId, playerName, playerNum, websocket) }
            context.watch(playerActor)
            Player(playerName, playerNum, playerActor)
        }

        val playersCycle: CycleList<Player> = playerActorsCollection.toCyclyList()!!

        val gameActor = context.actorOf("game-$gameId"){ GameActor(gameId, playersCycle, websocket) }

        val inGameIndexItem = InGameIndexItem(gameActor)
        playerNames.forEach{ playersToGameIndex[it] = inGameIndexItem }

        tellAfter(1.sec(), gameActor){ StartGame() }
    }

    private fun findPlayersForNewGame(): Collection<String>? {
        log().debug("findPlayersForNewGame")
        //todo: Make a real implementation
        if (gameRequestsQueue.size < 2) return null

        val player1 = gameRequestsQueue.remove()!!.userName
        val player2 = gameRequestsQueue.remove()!!.userName

        return listOf( player1, player2 )
    }


}

