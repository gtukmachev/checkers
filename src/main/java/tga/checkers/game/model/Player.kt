package tga.checkers.game.model


data class GameRequest(
        val player: Player
)

data class Player(
        val userId: Int,
        val name: String,
        val gameRole: String
)
