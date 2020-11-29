package tga.checkers.game.actors

import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import akka.japi.pf.ReceiveBuilder
import org.springframework.messaging.simp.SimpMessagingTemplate
import tga.checkers.exts.CycleList
import tga.checkers.exts.on
import tga.checkers.exts.sec
import tga.checkers.exts.tellToSelfAfter
import tga.checkers.game.*

interface GameActorMessage
         class StartGame: GameActorMessage
         class FirstTurn: GameActorMessage
    data class NotifyPlayer(val userName: String): GameActorMessage

data class Player(
        val name: String,
        val index: Int,
        val actor: ActorRef
) {
    fun toPlayerInfo(): PlayerInfo = PlayerInfo(name = name, index = index, colorOf(index))
}

class GameActor(
        val gameId: Int,
        val playersCycle: CycleList<Player>,
        val websocket: SimpMessagingTemplate
) : AbstractLoggingActor() {

    private var activePlayerNode: CycleList<Player> = playersCycle
    private val activePlayer: Player get() = activePlayerNode.obj

    private var currentState: GameState = GameState.initialState()
    private var history: List<GameState> = listOf()

    override fun preStart() {
        log().debug("preStart")
        playersCycle.forEach{ it.actor.tell(JoinToGame(self), self) }
    }

    override fun createReceive(): Receive = gameStartingBehavior


    private val gameStartingBehavior = ReceiveBuilder()
            .on(StartGame::class   ){ onStartGame() }
            .on(FirstTurn::class   ){ onFirstTurn() }
            .build()


    private val waitingForStepsBehavior = ReceiveBuilder()
            .on(PlayerStep::class, { sender == activePlayer.actor }){ onPlayerStep(it)              }
            .on(PlayerStep::class                                  ){ onWrongPlayerStep(it, sender) }
            .on(NotifyPlayer::class                                ){ onNotifyPlayer(it)            }
            .build()


    private fun onNotifyPlayer(notifyPlayerMsg: NotifyPlayer) {
        val pl = playersCycle.firstOrNull{ it.name == notifyPlayerMsg.userName }
        pl?.let{ sendGameInfoToPlayer(it) }
    }

    private fun onStartGame() {
        log().debug("onStartGame")
        playersCycle.forEach( ::sendGameInfoToPlayer )

        tellToSelfAfter( 1.sec() ){ FirstTurn() }
    }

    private fun sendGameInfoToPlayer(pl: Player) {
        log().debug("sendGameInfoToPlayer(pl={})", pl)
        val gameInfo = GameInfo(
                gameId  = gameId,
                you     = pl.toPlayerInfo(),
                players = playersCycle.map{ it.toPlayerInfo() },
                gameStatus = GameStatus(
                        currentState = currentState,
                        history      = history
                )
        )
        log().debug("sendGameInfoToPlayer(pl={}) => gameInfo={}", gameInfo)
        pl.actor.tell(gameInfo, self)
    }

    private fun onFirstTurn() {
        context.become( waitingForStepsBehavior )
    }


    private fun onPlayerStep(playerStep: PlayerStep) {
        log().debug("onPlayerStep(playerStep={})", playerStep)
        this.history = history + currentState        // save the current state to history. IMPORTANT: the list should be immutable - don't change it!

        // process the active player step
        this.currentState = performPlayerStep(playerStep){
            // this callback will be invoked only and only if the current step is proceed without any errors
            switchActivePlayerToNextOne()
            activePlayer.index
        }

        checkIfPreviousStepWinTheGame()
        checkIfCurrentPlayerLooseTheGame()
        notifyPlayersAboutStepResult()

    }

    private fun performPlayerStep(playerStep: PlayerStep, calculateNextPlayer: () -> Int): GameState {
        log().debug("performPlayerStep({})", playerStep)
        //TODO("Not yet implemented")

        val step = Step(
                start = P(playerStep.lin, playerStep.col),
                shot = null,
                shotFigure = null,
                end = P(playerStep.lin + 1, playerStep.col + 1)
        )
        val move = Move(activePlayer.index, Figure.b, listOf(step), MoveStatus.OK)

        val newActivePlayerIndex: Int = calculateNextPlayer()
        val newGameState = GameState(
                nTurn = currentState.nTurn + 1,
                activePlayer = newActivePlayerIndex,
                lastMove = move,
                field = currentState.field
        )

        return newGameState
    }


    private fun switchActivePlayerToNextOne() {
        log().debug("switchActivePlayerToNextOne()")
        activePlayerNode = activePlayerNode.next!!
    }

    private fun checkIfCurrentPlayerLooseTheGame() {
        log().debug("checkIfCurrentPlayerLooseTheGame()")
        //TODO("Not yet implemented")
    }

    private fun checkIfPreviousStepWinTheGame() {
        log().debug("checkIfPreviousStepWinTheGame()")
        //TODO("Not yet implemented")
    }

    private fun notifyPlayersAboutStepResult() {
        log().debug("notifyActiveUserAboutHisStep()")
        playersCycle.forEach{
            it.actor.tell(currentState, self)
        }
    }


    private fun onWrongPlayerStep(playerStep: PlayerStep, wrongPlayerActor: ActorRef) {
        log().debug("onWrongPlayerStep(playerStep={}, wrongPlayerActor={})", playerStep, wrongPlayerActor)
        wrongPlayerActor.tell(ItIsNotYourStepError(), self)

    }

}

