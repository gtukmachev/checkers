package tga.checkers.game.actors

import tga.checkers.game.*


data class WebServiceOutcomeMessage(val gameId: Int, val msgType: String, val msg: ToPlayerMessage)

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
interface ToPlayerMessage

    object WaitingForAGame : ToPlayerMessage

    class ItIsNotYourStepError : ToPlayerMessage

    data class GameInfo(
        val gameId: Int,
        val players: Collection<PlayerInfo>,
        val you: Int,
        val board: Board,
        val history: GameHistory
    ) : ToPlayerMessage

    data class PlayerInfo(
        val index: Int,
        val name: String,
        val color: FigureColor = colorOfPlayerByIndex(index),
    ) : ToPlayerMessage

    data class NextMoveInfo(
        val newBoard: Board,
        val lastMove: PlayerMove
    ) : ToPlayerMessage

    data class WrongMoveError(
        val move: PlayerMove
    ) : ToPlayerMessage

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
interface WebServiceIncomeMessage

    data class PlayerMoveInfo(
        val turn: Int,
        val cellsQueue: List<P>
    ) : WebServiceIncomeMessage

    data class ResetGameMessage (val turn: Int) : WebServiceIncomeMessage
    data class ResignGameMessage(val turn: Int) : WebServiceIncomeMessage

