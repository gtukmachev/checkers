import { Component, OnDestroy, OnInit } from '@angular/core';
import { GameMakerService } from 'app/core/game-maker/game-maker.service';
import { Subscription } from 'rxjs';
import {
    GameInfo,
    ItIsNotYourStepError,
    NextMoveInfo,
    PlayerInfo,
    PlayerMoveInfo,
    ResetGameMessage,
    ResignGameMessage,
    ToPlayerMessage,
    WaitingForAGame,
    WebServiceOutcomeMessage,
    WrongMoveError,
} from 'app/core/game-maker/ExternalMessages';
import { ClassCastException } from 'app/core/game-maker/exceptions';
import { Board, BoardHistoryItem, Desk, GameHistory, P } from 'app/core/game-maker/GameStateData';

@Component({
    selector: 'jhi-game-page',
    templateUrl: './game-page.component.html',
    styleUrls: ['./game-page.component.scss'],
})
export class GamePageComponent implements OnInit, OnDestroy {
    // constants
    public static initialDesk: Desk = Desk.initialDesk(8, 8, 2);
    public colChar: string[] = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];

    public incomeMessages: WebServiceOutcomeMessage[] = [];
    public counter: number = 0;

    public gameId: number = -1;
    public players: PlayerInfo[] = [];
    public board: Board = Board.initialBoard(GamePageComponent.initialDesk);
    public history: GameHistory = [];

    public me: PlayerInfo | null = null;
    public opponent: PlayerInfo | null = null;

    private gameSubscription?: Subscription;

    constructor(private gameMakerService: GameMakerService) {
        console.trace('GamePageComponent.constructor():');
    }

    ngOnInit(): void {
        console.trace('GamePageComponent.ngOnInit():');
        this.gameSubscription = this.gameMakerService.userGameChannel.subscribe((msg: ToPlayerMessage) => this.onGameMessage(msg));
    }

    ngOnDestroy(): void {
        console.trace('GamePageComponent.ngOnDestroy():');
        this.gameSubscription?.unsubscribe();
    }

    private onGameMessage(msg: ToPlayerMessage): void {
        console.trace('GamePageComponent.onGameMessage():', msg);
             if (msg instanceof WaitingForAGame) this.onMsg_WaitingForAGame(msg);
        else if (msg instanceof ItIsNotYourStepError) this.onMsg_ItIsNotYourStepError(msg);
        else if (msg instanceof NextMoveInfo) this.onMsg_NextMoveInfo(msg);
        else if (msg instanceof WrongMoveError) this.onMsg_WrongMoveError(msg);
        else if (msg instanceof GameInfo) this.onMsg_GameInfo(msg);
        else
            throw new ClassCastException(
                `The type "${msg}" is unrecognized! Supported types are:` +
                    '[WaitingForAGame, ItIsNotYourStepError, NextMoveInfo, WrongMoveError, GameInfo]',
                msg
            );
    }

    private onMsg_WrongMoveError(wrongMoveError: WrongMoveError): void {
        console.trace('GamePageComponent.onMsg_WrongMoveError():', wrongMoveError);
    }

    private onMsg_NextMoveInfo(nextMoveInfo: NextMoveInfo): void {
        console.trace('GamePageComponent.onMsg_NextMoveInfo():', nextMoveInfo);
        const expectedNextTurnNumber = this.board.turn + 1;
        const nextTurnNumber = nextMoveInfo.newBoard.turn;

        if (expectedNextTurnNumber !== nextTurnNumber) {
            // something wrong!!!!
            console.warn(
                `The game state is unsynchronized ( expected next turn number is '${expectedNextTurnNumber}', but the  received is '${nextTurnNumber}') ! Requesting full game info from server...`
            );
            this.gameMakerService.updateGameStateRequest();
            return;
        }

        this.history.push(new BoardHistoryItem(this.board, nextMoveInfo.lastMove));
        this.board = nextMoveInfo.newBoard;
    }

    private onMsg_GameInfo(gameInfoMsg: GameInfo): void {
        console.trace('GamePageComponent.onMsg_GameInfo():', gameInfoMsg);
        this.gameId = gameInfoMsg.gameId;
        this.players = gameInfoMsg.players;
        this.board = gameInfoMsg.board;
        this.history = gameInfoMsg.history;

        let opponentIndex = gameInfoMsg.you === 0 ? 1 : 0;
        this.me = gameInfoMsg.players[gameInfoMsg.you];
        this.opponent = gameInfoMsg.players[opponentIndex];
    }

    private onMsg_ItIsNotYourStepError(itIsNotYourStepError: ItIsNotYourStepError): void {
        console.trace('GamePageComponent.onMsg_ItIsNotYourStepError():', itIsNotYourStepError);

        // This means - my current state is broken.
        // Probably, due some connection issues and loosing a number of income messages
        // Request the current state of the game from the server (the answer will be handled via the onMsg_GameInfo() method:
        this.gameMakerService.updateGameStateRequest();
    }

    private onMsg_WaitingForAGame(waitingForAGame: WaitingForAGame): void {
        console.trace('GamePageComponent.onMsg_WaitingForAGame():', waitingForAGame);
    }

    startGameRequest(): void {
        this.gameMakerService.findGame();
    }
    resetGameRequest(): void {
        this.gameMakerService.resetGame(new ResetGameMessage(this.board.turn));
    }
    resignGameRequest(): void {
        this.gameMakerService.resignGame(new ResignGameMessage(this.board.turn));
    }

    sendStepToServer(cellsQueue: P[]): void {
        console.trace('GamePageComponent.sendStepToServer():', cellsQueue);

        const playerMoveInfo = new PlayerMoveInfo(this.board.turn, cellsQueue);
        this.gameMakerService.sendStep(playerMoveInfo);
    }
}
