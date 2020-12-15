import { Component, OnDestroy, OnInit } from '@angular/core';
import { GameMakerService } from 'app/core/game-maker/game-maker.service';
import {
    AllFiguresOnBoard,
    ClassCastException,
    Field,
    FigureColor,
    FigureOnBoard,
    FigureType,
    GameInfo,
    GameMessage,
    GameState,
    GameStatus,
    initialGameState,
    ItIsNotYourStepError,
    P,
    PlayerInfo,
    WaitingForAGame,
} from 'app/core/game-maker/GameMessages';
import { Subscription } from 'rxjs';
import { IMove } from 'app/core/game-maker/IMove';

@Component({
    selector: 'jhi-game-page',
    templateUrl: './game-page.component.html',
    styleUrls: ['./game-page.component.scss'],
})
export class GamePageComponent implements OnInit, OnDestroy {
    public incomeMessages: GameMessage[] = [];
    public counter: number = 0;

    public gameId: number = -1;
    public currentState: GameState = initialGameState();
    public history: GameState[] = [];
    public me: PlayerInfo | null = null;
    public players: PlayerInfo[] = [];
    public opponent: PlayerInfo | null = null;

    public colChar: string[] = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];

    private gameSubscription?: Subscription;
    public figures: AllFiguresOnBoard = new Map<FigureColor, FigureOnBoard[]>();

    constructor(private gameMakerService: GameMakerService) {}

    ngOnInit(): void {
        this.gameSubscription = this.gameMakerService.userGameChannel.subscribe((msg: GameMessage) => this.onGameMessage(msg));
    }

    ngOnDestroy(): void {
        this.gameSubscription?.unsubscribe();
    }

    private onGameMessage(msg: GameMessage): void {
        console.log('onGameMessage:', msg);
        switch (msg.msgType) {
            case 'GameState':
                this.onMsg_GameState(msg.msg as GameState);
                break;
            case 'GameStatus':
                this.onMsg_GameStatus(msg.msg as GameStatus);
                break;
            case 'ItIsNotYourStepError':
                this.onMsg_ItIsNotYourStepError(msg.msg as ItIsNotYourStepError);
                break;
            case 'WaitingForAGame':
                this.onMsg_WaitingForAGame(msg.msg as WaitingForAGame);
                break;
            case 'GameInfo':
                this.onMsg_GameInfo(msg.msg as GameInfo);
                break;
            default:
                throw new ClassCastException(
                    `The type "${msg.msgType}" is unrecognized! Supported types are: [GameState, GameStatus, ItIsNotYourStepError, WaitingForAGame, GameInfo]`
                );
        }
    }

    private onMsg_GameState(gameState: GameState): void {
        this.history.push(this.currentState);
        this.currentState = gameState;

        this.figures = this.loadFigures(this.currentState.field);
    }

    private onMsg_GameStatus(gameStatusMsg: GameStatus): void {
        this.currentState = gameStatusMsg.currentState;
        this.history = gameStatusMsg.history;

        this.figures = this.loadFigures(this.currentState.field);
    }

    private onMsg_GameInfo(gameInfoMsg: GameInfo): void {
        this.gameId = gameInfoMsg.gameId;
        this.me = gameInfoMsg.you;
        this.players = gameInfoMsg.players;
        this.onMsg_GameStatus(gameInfoMsg.gameStatus);
        if (this.players[0].index === this.me.index) {
            this.opponent = this.players[1];
        } else {
            this.opponent = this.players[0];
        }
    }

    private onMsg_ItIsNotYourStepError(itIsNotYourStepError: ItIsNotYourStepError): void {
        console.trace('onMsg_ItIsNotYourStepError():', itIsNotYourStepError);

        // This means - my current state is broken.
        // Probably, due some connection issues and loosing a number of income messages
        // Request the current state of the game from the server (the answer will be handled via the onMsg_GameInfo() method:
        this.gameMakerService.updateGameStateRequest();
    }

    private onMsg_WaitingForAGame(waitingForAGame: WaitingForAGame): void {
        console.trace('onMsg_WaitingForAGame():', waitingForAGame);
    }

    private loadFigures(field: Field): AllFiguresOnBoard {
        let fs = new Map<FigureColor, FigureOnBoard[]>();
        let black: FigureOnBoard[] = [];
        let white: FigureOnBoard[] = [];

        fs.set(FigureColor.WHITE, white);
        fs.set(FigureColor.BLACK, black);

        let i = 0;
        for (let l = 0; l < 8; l++) {
            for (let c = 0; c < 8; c++) {
                const figure = field.desk[i];
                const f: FigureOnBoard = { l: l, c: c, isQuinn: figure?.type === FigureType.QUINN, isActive: false };
                switch (figure?.color) {
                    case FigureColor.WHITE: {
                        white.push(f);
                        break;
                    }
                    case FigureColor.BLACK: {
                        black.push(f);
                        break;
                    }
                }
                i++;
            }
        }

        return fs;
    }

    startGameRequest(): void {
        this.gameMakerService.findGame();
    }

    sendStepToServer(cellsQueue: P[]): void {
        console.log('sendStepToServer:', cellsQueue);
        const step: IMove = {
            turn: this.currentState.turn,
            cellsQueue: cellsQueue,
        };
        this.gameMakerService.sendStep(step);
    }
}
