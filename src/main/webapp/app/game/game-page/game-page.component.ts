import { Component, OnDestroy, OnInit } from '@angular/core';
import { GameMakerService } from 'app/core/game-maker/game-maker.service';
import {
    AllFiguresOnBoard,
    ClassCastException,
    Figure,
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
import { IStep } from 'app/core/game-maker/IStep';

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
    public figures: AllFiguresOnBoard = new Map();

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
                    `Type of inner object "${msg.msgType}" is unrecognized! Supported types are: [GameStatus, ItIsNotYourStepError, WaitingForAGame, GameInfo]`
                );
        }
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

    startGameRequest(): void {
        this.gameMakerService.findGame();
    }

    sendStepToServer(cellsQueue: P[]): void {
        console.log('sendStepToServer:', cellsQueue);
        const step: IStep = {
            nTurn: this.currentState.nTurn,
            cellsQueue: cellsQueue,
        };
        this.gameMakerService.sendStep(step);
    }

    private loadFigures(field: (Figure | null)[][]): AllFiguresOnBoard {
        let fs = new Map<FigureColor, FigureOnBoard[]>();
        let black: FigureOnBoard[] = [];
        let white: FigureOnBoard[] = [];

        fs.set(FigureColor.WHITE, white);
        fs.set(FigureColor.BLACK, black);

        field?.forEach((row, l) => {
            row?.forEach((figure, c) => {
                switch (figure?.color) {
                    case FigureColor.WHITE: {
                        white.push({ l: l, c: c, isQuinn: figure?.type === FigureType.QUINN, isActive: false });
                        break;
                    }
                    case FigureColor.BLACK: {
                        black.push({ l: l, c: c, isQuinn: figure?.type === FigureType.QUINN, isActive: false });
                        break;
                    }
                }
            });
        });

        return fs;
    }
}
