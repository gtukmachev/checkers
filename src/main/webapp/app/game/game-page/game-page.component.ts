import { Component, OnDestroy, OnInit } from '@angular/core';
import { GameMakerService } from 'app/core/game-maker/game-maker.service';
import {
    ClassCastException,
    GameInfo,
    GameMessage,
    GameStatus,
    ItIsNotYourStepError,
    WaitingForAGame,
} from 'app/core/game-maker/GameMessages';
import { Subscription } from 'rxjs';

@Component({
    selector: 'jhi-game-page',
    templateUrl: './game-page.component.html',
    styleUrls: ['./game-page.component.scss'],
})
export class GamePageComponent implements OnInit, OnDestroy {
    incomeMessages: GameMessage[] = [];
    counter: number = 0;

    private gameSubscription?: Subscription;

    constructor(private gameMakerService: GameMakerService) {}

    ngOnInit(): void {
        this.gameSubscription = this.gameMakerService.userGameChannel.subscribe((msg: GameMessage) => this.onGameMessage(msg));
    }

    ngOnDestroy(): void {
        this.gameSubscription?.unsubscribe();
    }

    private onGameMessage(msg: GameMessage) {
        this.incomeMessages.push(msg);
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

    private onMsg_GameStatus(gameStatus: GameStatus) {}

    private onMsg_ItIsNotYourStepError(itIsNotYourStepError: ItIsNotYourStepError) {}

    private onMsg_WaitingForAGame(waitingForAGame: WaitingForAGame) {}

    private onMsg_GameInfo(gameInfo: GameInfo) {}

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
