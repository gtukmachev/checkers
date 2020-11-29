package tga.checkers.game

import tga.checkers.game.actors.ToPlayerMessage

enum class FigureType{ STONE, QUINN }
enum class FigureColor{ BLACK, WHITE }
enum class Figure(val type: FigureType, val color: FigureColor) {
    b(FigureType.STONE, FigureColor.BLACK),
    w(FigureType.STONE, FigureColor.WHITE),
    bq(FigureType.QUINN, FigureColor.BLACK),
    wq(FigureType.QUINN, FigureColor.WHITE),
}

fun colorOf(index: Int): FigureColor = FigureColor.values()[index]

data class GameState(
        val nTurn: Int,
        val activePlayer: Int,
        val lastMove: Move,
        val field: Array<Array<Figure?>>,
) : ToPlayerMessage {
    companion object {
        private val o: Figure? = null
        private val w = Figure.w
        private val b = Figure.b
        private val noMove = Move(-1, Figure.w, listOf(), MoveStatus.EMPTY)

        fun initialState(): GameState = GameState(
                nTurn = 0,
                activePlayer = 0,
                lastMove = noMove,
                field = arrayOf(
                    arrayOf(w, o, w, o, w, o, w, o),
                    arrayOf(o, w, o, w, o, w, o, w),
                    arrayOf(w, o, w, o, w, o, w, o),
                    arrayOf(o, o, o, o, o, o, o, o),
                    arrayOf(o, o, o, o, o, o, o, o),
                    arrayOf(o, b, o, b, o, b, o, w),
                    arrayOf(b, o, b, o, b, o, b, o),
                    arrayOf(o, b, o, b, o, b, o, w),
                )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GameState) return false

        if (nTurn != other.nTurn) return false
        if (!field.contentDeepEquals(other.field)) return false
        if (activePlayer != other.activePlayer) return false
        if (lastMove != other.lastMove) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nTurn
        result = 31 * result + field.contentDeepHashCode()
        result = 31 * result + activePlayer
        result = 31 * result + lastMove.hashCode()
        return result
    }
}

data class Move(
        val player: Int,
        val figure: Figure,
        val steps: List<Step>,
        val status: MoveStatus
)

enum class MoveStatus{ EMPTY, OK, ERROR }

data class Step(
        val start: P,
        val shot: P?,
        val shotFigure: Figure?,
        val end: P,
)

data class P(
        val l: Int, 
        val c: Int
)
