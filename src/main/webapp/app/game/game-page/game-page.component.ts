import { Component, OnDestroy, OnInit } from '@angular/core';
import { GameMakerService } from 'app/core/game-maker/game-maker.service';
import {
    ClassCastException,
    GameInfo,
    GameMessage,
    GameState,
    GameStatus,
    initialGameState,
    ItIsNotYourStepError,
    PlayerInfo,
    WaitingForAGame,
} from 'app/core/game-maker/GameMessages';
import { Subscription } from 'rxjs';

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

    public colChar: string[] = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];

    private gameSubscription?: Subscription;

    constructor(private gameMakerService: GameMakerService) {}

    ngOnInit(): void {
        this.gameSubscription = this.gameMakerService.userGameChannel.subscribe((msg: GameMessage) => this.onGameMessage(msg));
    }

    ngOnDestroy(): void {
        this.gameSubscription?.unsubscribe();
    }

    private onGameMessage(msg: GameMessage) {
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
                    `Type of inner object ${msg.msgType} is unrecognized! Supported types are: [GameStatus, ItIsNotYourStepError, WaitingForAGame, GameInfo]`
                );
        }
    }

    private onMsg_GameStatus(gameStatusMsg: GameStatus) {
        this.currentState = gameStatusMsg.currentState;
        this.history = gameStatusMsg.history;
    }

    private onMsg_GameInfo(gameInfoMsg: GameInfo) {
        this.gameId = gameInfoMsg.gameId;
        this.me = gameInfoMsg.you;
        this.players = gameInfoMsg.players;
        this.onMsg_GameStatus(gameInfoMsg.gameStatus);
    }

    private onMsg_ItIsNotYourStepError(itIsNotYourStepError: ItIsNotYourStepError) {
        // This means - my current state is broken.
        // Probably, due some connection issues and loosing a number of income messages
        // Request the current state of the game from the server (the answer will be handled via the onMsg_GameInfo() method:
        this.gameMakerService.updateGameStateRequest();
    }

    private onMsg_WaitingForAGame(waitingForAGame: WaitingForAGame) {}

    startGameRequest() {
        this.gameMakerService.findGame();
    }

    sendStepToServer() {
        this.counter += 1;
        const step = {
            lin: this.counter,
            col: this.counter,
        };
        this.gameMakerService.sendStep(step);
    }

    clearLog() {
        this.incomeMessages = [];
    }
}
