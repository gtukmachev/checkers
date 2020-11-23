package tga.checkers.game.actors

import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import akka.japi.pf.ReceiveBuilder
import org.springframework.messaging.simp.SimpMessagingTemplate
import tga.checkers.exts.CycleList
import tga.checkers.exts.on
import tga.checkers.exts.sec
import tga.checkers.exts.tellAfter

interface GameActorMessage
         class StartGame: GameActorMessage
         class FirstTurn: GameActorMessage
    data class NotifyPlayer(val userName: String): GameActorMessage

data class Player(
        val name: String,
        val num: Int,
        val actor: ActorRef
)

class GameActor(
        val gameId: Int,
        val playersCycle: CycleList<Player>,
        val websocket: SimpMessagingTemplate
) : AbstractLoggingActor() {

    var activePlayerNode: CycleList<Player> = playersCycle

    val player: Player get() = activePlayerNode.obj

    var nTurn: Int = 0

    override fun preStart() {
        log().debug("preStart")
        playersCycle.forEach{ it.actor.tell(JoinToGame(self), self) }
    }

    override fun createReceive(): Receive = gameStartingBehavior

    private val gameStartingBehavior = ReceiveBuilder()
            .on(StartGame::class   ){ onStartGame() }
            .on(FirstTurn::class   ){ onFirstTurn() }
            .build()


    private val waitingForStepBehavior = ReceiveBuilder()
            .on(PlayerStep::class, { sender == player.actor }){ onPlayerStep(it)              }
            .on(PlayerStep::class                            ){ onWrongPlayerStep(it, sender) }
            .on(NotifyPlayer::class                          ){ onNotifyPlayer(it)            }
            .build()

    private fun onNotifyPlayer(notifyPlayerMsg: NotifyPlayer) {
        val pl = playersCycle.firstOrNull{ it.name == notifyPlayerMsg.userName }
        pl?.let{ sendGameStatusToPlayer(it) }
    }

    private fun onStartGame() {
        log().debug("onStartGame")
        playersCycle.forEach( ::sendGameStatusToPlayer )

        tellAfter( 1.sec() ){ FirstTurn() }
    }

    private fun sendGameStatusToPlayer(pl: Player) {
        val users = playersCycle.map { it.name }
        val msg = GameStatus(
                gameId = gameId,
                players = users,
                activePlayer = player.name
        )
        pl.actor.tell(msg, self)
    }

    private fun onFirstTurn() = nextTurn()

    private fun nextTurn() {
        nTurn++
        log().debug("nextTurn() : {}", nTurn)

        switchActivePlayer()
        checkIfCurrentPlayerLooseTheGame()
        notifyActiveUserAboutHisStep()
        context.become( waitingForStepBehavior )
    }

    private fun onPlayerStep(playerStep: PlayerStep) {
        log().debug("onPlayerStep(playerStep={})", playerStep)
        nextTurn()
    }

    private fun switchActivePlayer() {
        log().debug("switchActivePlayer()")
        activePlayerNode = activePlayerNode.next!!
    }

    private fun checkIfCurrentPlayerLooseTheGame() {
        log().debug("checkIfCurrentPlayerLooseTheGame()")
        //TODO("Not yet implemented")
    }

    private fun notifyActiveUserAboutHisStep() {
        log().debug("notifyActiveUserAboutHisStep()")
        player.actor.tell( YourStep(nTurn), self)
    }

    private fun onWrongPlayerStep(playerStep: PlayerStep, wrongPlayerActor: ActorRef) {
        log().debug("onWrongPlayerStep(playerStep={}, wrongPlayerActor={})", playerStep, wrongPlayerActor)
        wrongPlayerActor.tell(ItIsNotYourStepError(), self)

    }

}
