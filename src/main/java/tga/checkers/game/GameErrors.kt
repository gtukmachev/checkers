package tga.checkers.game

abstract class GameError(msg: String = "", cause: Throwable? = null) : RuntimeException(msg, cause)

class OutOfBoardError(val p: P) : GameError(
    "The point you are working with is out of the board: ${p.toHumanCoordinates()}"
)