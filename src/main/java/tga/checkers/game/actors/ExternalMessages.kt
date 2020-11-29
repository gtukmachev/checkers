package tga.checkers.game.actors

import tga.checkers.game.FigureColor
import tga.checkers.game.GameState


data class WebServiceOutcomeMessage(val gameId: Int, val msgType: String, val msg: ToPlayerMessage)
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
interface ToPlayerMessage

    object WaitingForAGame : ToPlayerMessage

    class ItIsNotYourStepError: ToPlayerMessage

    data class GameInfo(
            val gameId: Int,
            val you: PlayerInfo,
            val players: Collection<PlayerInfo>,
            val gameStatus: GameStatus
            ) : ToPlayerMessage

    data class PlayerInfo(
            val name: String,
            val index: Int,
            val color: FigureColor,
    )

    data class GameStatus(
            val currentState: GameState,
            val history: List<GameState> ) : ToPlayerMessage

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
interface WebServiceIncomeMessage
    data class PlayerStep(val lin: Int, val col: Int) : WebServiceIncomeMessage
