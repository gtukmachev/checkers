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
    data class NotifyPlayer(val userName: String): GameActorMessage, WebServiceIncomeMessage

data class Player(
        val name: String,
        val index: Int,
        val actor: ActorRef
) {
    fun toPlayerInfo(): PlayerInfo = PlayerInfo(name = name, index = index)
}

class GameActor(
        val gameId: Int,
        val playersCycle: CycleList<Player>,
        val websocket: SimpMessagingTemplate
) : AbstractLoggingActor() {

    // the game state
    private var activePlayerNode: CycleList<Player> = playersCycle

    private var gameStatus: GameStatus = GameStatus.ACTIVE
    private val activePlayer: Player get() = activePlayerNode.obj
    private val desk: Desk = Desk.initialDesk(lines = 8, columns = 8, players = 2)
    private var board: Board = Board.initialBoard(desk)
    private var history: GameHistory = listOf()

    override fun preStart() {
        log().debug("preStart")

        // todo: load the game state from a DB

        playersCycle.forEach{ it.actor.tell(JoinToGame(self), self) }
    }

    override fun createReceive(): Receive = gameStartingBehavior

    private val gameStartingBehavior = ReceiveBuilder()
            .on(StartGame::class   ){ onStartGame() }
            .on(FirstTurn::class   ){ onFirstTurn() }
            .build()


    private val waitingForStepsBehavior = ReceiveBuilder()
            .on(PlayerMoveInfo::class, { sender == activePlayer.actor }){ onPlayerMoveInfo(it)          }
            .on(PlayerMoveInfo::class                                  ){ onWrongPlayerStep(it, sender) }
            .on(NotifyPlayer::class                                    ){ onNotifyPlayer(it)            }
            .on(ResetGameMessage::class                                ){ onResetGameMessage(it)        }
            .on(ResignGameMessage::class                               ){ onResignGameMessage(it)       }
            .build()

    private fun onNotifyPlayer(notifyPlayerMsg: NotifyPlayer) {
        val pl = playersCycle.firstOrNull{ it.name == notifyPlayerMsg.userName }
        pl?.let{ sendGameInfoToPlayer(it) }
    }

    private fun onResetGameMessage(resetGameMessage: ResetGameMessage) {
        log().debug("onResetGameMessage({})", resetGameMessage)
        desk.reset()
        board = Board.initialBoard(desk)
        history = listOf()
        playersCycle.forEach { sendGameInfoToPlayer(it) }
    }

    private fun onResignGameMessage(resignGameMessage: ResignGameMessage) {
        log().debug("onResetGameMessage({})", resignGameMessage)
        //TODO("Not yet implemented")
    }

    private fun onStartGame() {
        log().debug("onStartGame")
        playersCycle.forEach( ::sendGameInfoToPlayer )

        tellToSelfAfter( 1.sec() ){ FirstTurn() }
    }

    private fun sendGameInfoToPlayer(pl: Player) {
        val gameInfo = GameInfo(
                gameId  = gameId,
                you     = pl.index,
                players = playersCycle.map{ it.toPlayerInfo() },
                board   = board,
                history = history
        )
        log().debug("sendGameInfoToPlayer(pl={}) => gameInfo={}", pl, gameInfo)
        pl.actor.tell(gameInfo, self)
    }

    private fun onFirstTurn() {
        context.become( waitingForStepsBehavior )
    }

    private fun onPlayerMoveInfo(playerMoveInfo: PlayerMoveInfo) {
        log().debug("onPlayerMoveInfo(playerStep={})", playerMoveInfo)

        performPlayerStep(
            playerMoveInfo = playerMoveInfo,
            onSuccess = this::finishCurrentStepSuccessfully,
            onError   = this::notifyCurrentPlayerAboutErrorMove
        )
    }

    private fun finishCurrentStepSuccessfully(playerMove: PlayerMove) {
        // this callback will be invoked only and only if the current step is proceed without any errors
        history = history + BoardHistoryItem(board, playerMove)

        val prevPlayer = activePlayer

        switchActivePlayerToNextOne()

        board = Board(
            turn = board.turn + 1,
            activePlayerIndex = activePlayer.index,
            figures = desk.mapToFiguresByPlayers()
        )

        if (gameStatus != GameStatus.FINISHED && checkIfPreviousStepWinTheGame()   ) notifyPlayersAboutWin  (playerMove, prevPlayer)
        if (gameStatus != GameStatus.FINISHED && checkIfCurrentPlayerLooseTheGame()) notifyPlayersAboutLoose(playerMove, activePlayer)
        if (gameStatus != GameStatus.FINISHED                                      ) notifyPlayersAboutSuccessMove(playerMove)
    }

    private fun notifyPlayersAboutLoose(playerMove: PlayerMove, looser: Player) {
        TODO("Not yet implemented")
    }

    private fun notifyPlayersAboutWin(playerMove: PlayerMove, winner: Player) {
        TODO("Not yet implemented")
    }

    private fun performPlayerStep(playerMoveInfo: PlayerMoveInfo,
                                  onSuccess: (PlayerMove) -> Unit,
                                  onError: (PlayerMove?, PlayerMoveInfo) -> Unit,
    ) {
        log().debug("performPlayerStep({})", playerMoveInfo)
        //TODO("Not yet implemented")

        var figure: Figure? = null
        val steps = mutableListOf<FigureStep>()

        for(i in playerMoveInfo.cellsQueue.indices) {
            when (i) {
                0 -> {
                    val p = playerMoveInfo.cellsQueue[i]
                    figure = desk.figureAt(p)
                }
                else -> {
                    val figureStep = FigureStep(
                        begin = playerMoveInfo.cellsQueue[i-1],
                        end = playerMoveInfo.cellsQueue[i],
                        shot = null
                    )
                    steps.add(figureStep)
                }
            }
        }

        val move = PlayerMove(activePlayer.index, figure!!, steps, null)

        applyMove(move)

        onSuccess(move)

    }

    private fun applyMove(playerMove: PlayerMove) {
        log().debug("applyMove({})", playerMove)
        playerMove.steps.forEach {
            if (log().isDebugEnabled) log().debug("applyMove(): ${it.begin.toHumanCoordinates()} -> ${it.end.toHumanCoordinates()}")
            desk[it.end] = desk[it.begin]
            desk[it.begin] = null
        }
    }

    private fun switchActivePlayerToNextOne() {
        log().debug("switchActivePlayerToNextOne()")
        activePlayerNode = activePlayerNode.next!!
    }

    private fun checkIfCurrentPlayerLooseTheGame(): Boolean {
        log().debug("checkIfCurrentPlayerLooseTheGame()")
        //TODO("Not yet implemented")
        return false
    }

    private fun checkIfPreviousStepWinTheGame(): Boolean {
        log().debug("checkIfPreviousStepWinTheGame()")
        //TODO("Not yet implemented")
        return false
    }

    private fun notifyPlayersAboutSuccessMove(playerMove: PlayerMove) {
        log().debug("notifyActiveUserAboutHisStep()")
        playersCycle.forEach{
            it.actor.tell(NextMoveInfo(newBoard = board, lastMove = playerMove), self)
        }
    }

    private fun notifyCurrentPlayerAboutErrorMove(playerMove: PlayerMove?, playerMoveInfo: PlayerMoveInfo) {
        TODO("Not yet implemented")
    }

    private fun onWrongPlayerStep(playerMoveInfo: PlayerMoveInfo, wrongPlayerActor: ActorRef) {
        log().debug("onWrongPlayerStep(playerMoveInfo={}, wrongPlayerActor={})", playerMoveInfo, wrongPlayerActor)
        wrongPlayerActor.tell(ItIsNotYourStepError(), self)

    }

}

