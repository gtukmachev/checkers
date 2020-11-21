package tga.checkers.game.actors

import akka.actor.AbstractLoggingActor
import akka.actor.ActorRef
import akka.japi.pf.ReceiveBuilder
import org.springframework.messaging.simp.SimpMessagingTemplate
import tga.checkers.exts.actorOf
import tga.checkers.exts.on
import tga.checkers.exts.sec
import tga.checkers.exts.tellAfter
import tga.checkers.game.model.Player

interface GameActorMessage
    class  StartGame: GameActorMessage
    class  FirstTurn: GameActorMessage


class GameActor(
        val gameId: Int,
        val players: Collection<Player>,
        val websocket: SimpMessagingTemplate
) : AbstractLoggingActor() {

    lateinit var playerActors: Array<ActorRef>

    var activePlayerActor: Int = -1
    var nTurn: Int = 0

    override fun preStart() {
        log().debug("preStart")
        playerActors = players.mapIndexed( ::createPlayerActor ).toTypedArray()
        tellAfter( 2.sec() ){ StartGame() }
    }

    private fun createPlayerActor(playerIndex: Int, player: Player): ActorRef {
        log().debug("createPlayerActor(playerIndex={}, player={})", playerIndex, player)
        return context.actorOf("player-${player.name}"){ PlayerActor(gameId, player, self, websocket) }
    }

    override fun createReceive(): Receive = gameStatingBehavior

    private val gameStatingBehavior = ReceiveBuilder()
            .on(StartGame::class){ onStartGame() }
            .on(FirstTurn::class){ onFirstTurn() }
            .build()

    private val waitingForStepBehavior = ReceiveBuilder()
            .on(PlayerStep::class, { sender == playerActors[activePlayerActor] }){ onPlayerStep(it)      }
            .on(PlayerStep::class,                                              ){ onWrongPlayerStep(it, sender) }
            .build()

    private fun onStartGame() {
        log().debug("onStartGame")
        playerActors.forEach { player ->  player.tell( GameHasBeenStarted(), self )  }

        tellAfter( 2.sec() ){ FirstTurn() }
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
        activePlayerActor++
        if (activePlayerActor >= playerActors.size) activePlayerActor = 0
    }

    private fun checkIfCurrentPlayerLooseTheGame() {
        log().debug("checkIfCurrentPlayerLooseTheGame()")
        //TODO("Not yet implemented")
    }

    private fun notifyActiveUserAboutHisStep() {
        log().debug("notifyActiveUserAboutHisStep()")
        val playerActor = playerActors[activePlayerActor]

        playerActor.tell( YourStep(nTurn), self)
    }

    private fun onWrongPlayerStep(playerStep: PlayerStep, wrongPlayerActor: ActorRef) {
        log().debug("onWrongPlayerStep(playerStep={}, wrongPlayerActor={})", playerStep, wrongPlayerActor)
        wrongPlayerActor.tell(ItIsNotYourStepError(), self)

    }

}
