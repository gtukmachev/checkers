package tga.checkers.game

enum class FigureType{ STONE, QUINN }
enum class FigureColor{ WHITE, BLACK, BLUE, GREEN, CYAN }

typealias PlayerIndex = Int
typealias GameHistory = List<BoardHistoryItem>

fun colorOfPlayerByIndex(index: PlayerIndex): FigureColor = FigureColor.values()[index]

/**
 * Coordinates **(Point)** on the game board
 *
 * 1. `l` = line
 * 2. `c` = column
 */
data class P(val l: Int, val c: Int) {

    constructor(plainBoardIndex: Int, boardLines: Int, boardColumns: Int) :
            this(
                l = plainBoardIndex / boardLines,
                c = plainBoardIndex % boardColumns
            )

    fun toHumanCoordinates() = "${l+1}${'A'+c}"
}


//Desk.initialDesk(lines = 8, columns = 8, players = 2)
data class Board(
    val turn: Int,
    val activePlayerIndex: PlayerIndex,
    val figures: Map<PlayerIndex, List<Figure>>,
) {
    companion object {
        fun initialBoard(desk: Desk): Board = Board(
            turn = 0,
            activePlayerIndex = 0,
            figures = desk.mapToFiguresByPlayers()
        )
    }
}

data class DeskFigure(val player: PlayerIndex, var type: FigureType, val id: Int)
data class Desk(
    val lines: Int,
    val columns: Int,
    val figures: Array<DeskFigure?>
) {

    operator fun set(l: Int, c: Int, value: DeskFigure?) {
        if (!isOnDesk(l, c)) throw OutOfBoardError( P(l,c) )
        figures[l*lines + c] = value
    }
    operator fun get(l: Int, c: Int): DeskFigure? {
        if (!isOnDesk(l, c)) throw OutOfBoardError( P(l,c) )
        return figures[l*lines + c]
    }

    operator fun set(p: P, value: DeskFigure?) = set(p.l, p.c, value)
    operator fun get(p: P) = get(p.l, p.c)

    fun isOnDesk(l: Int, c: Int) = ((l in 0 until lines) && (c in 0 until columns))
    fun isOnDesk(p: P) = isOnDesk(p.l, p.c)

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    fun mapToFiguresByPlayers(): Map<PlayerIndex, List<Figure>> {
        val pairsPlayerToFigure = figures.asSequence()
            .mapIndexed { i, f ->
                if (f == null) {
                    null
                } else {
                    f.player to Figure(id = f.id, p = P(i, 8, 8), type = f.type)
                }
            }.filterNotNull()

        return pairsPlayerToFigure.groupBy({it.first}, {it.second})

    }

    fun figureAt(l: Int, c: Int): Figure? {
        val deskFigure = this[l,c] ?: return null
        return Figure(
            id = deskFigure.id,
            p = P(l,c),
            type = deskFigure.type
        )
    }
    fun figureAt(p: P) = figureAt(p.l, p.c)

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Desk) return false

        if (!figures.contentEquals(other.figures)) return false

        return true
    }

    override fun hashCode(): Int {
        return figures.contentHashCode()
    }

    companion object {
        fun initialDesk(lines: Int, columns: Int, players: Int): Desk{
            val arr = Array<DeskFigure?>(lines*columns){ null }

            if (players !in 1..2) throw RuntimeException("Only 2 players games are supported now!")

            val desk = Desk(lines, columns, arr)

            var nextFigureId = 0

            // 3 first of WHITE in 2x2 game
            for (l in 0..2) for (c in 0..columns) if ( (l+c) % 2 == 0){
                desk[l,c] = DeskFigure(0, FigureType.STONE, nextFigureId++)
            }

            // 3 last rows of BLACK in 2x2 game
            for (l in (lines-3) until lines) for (c in 0..columns) if ( (l+c) % 2 == 0){
                desk[l,c] = DeskFigure(1, FigureType.STONE, nextFigureId++)
            }

            return desk
        }
    }

}

data class Figure(
    val id: Int,
    val p: P,
    val type: FigureType
)
data class BoardHistoryItem(
    val before: Board,
    val move: PlayerMove
)

data class PlayerMove(
    val playerIndex: PlayerIndex,
    val figure: Figure,
    val steps: List<FigureStep>,
    val err: String?
)

data class FigureStep(
    val begin: P,
    val end: P,
    val shot: Figure?
)
