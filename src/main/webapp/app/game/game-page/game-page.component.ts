import { Component, OnDestroy, OnInit } from '@angular/core';
import { GameMakerService } from 'app/core/game-maker/game-maker.service';
import { IFoundGameDescriptor } from 'app/core/game-maker/IFoundGameDescriptor';
import { IServerMessage } from 'app/game/game-page/IServerMessage';
import { IGameMessage } from 'app/core/game-maker/IGameMessage';

@Component({
    selector: 'jhi-game-page',
    templateUrl: './game-page.component.html',
    styleUrls: ['./game-page.component.scss'],
})
export class GamePageComponent implements OnInit, OnDestroy {
    gameDescriptor: IFoundGameDescriptor | null = null;
    serverMessages: IServerMessage[] | null = null;
    counter: number = 0;

    constructor(private gameMakerService: GameMakerService) {}

    ngOnInit(): void {}

    ngOnDestroy(): void {
        if (this.gameDescriptor) {
            this.gameMakerService.unsubscribeFromGameIfAny(this.gameDescriptor);
        }
    }

    startGameRequest() {
        this.gameMakerService.findGame(
            e => this.onGameFound(e),
            e => this.onGameMessage(e)
        );
    }

    onGameFound(gameDescriptor: IFoundGameDescriptor) {
        this.gameDescriptor = gameDescriptor;
        console.log('Game found: ', this.gameDescriptor);
        this.serverMessages = [
            {
                messageChannel: '/user/queue/new-game-request',
                msg: this.gameDescriptor,
            },
        ];
    }

    onGameMessage(gameMessage: IGameMessage) {
        this.serverMessages?.push({
            messageChannel: `/user/queue/game/${this.gameDescriptor?.gameId}`,
            msg: gameMessage,
        });
    }

    sendStepToServer() {
        if (this.gameDescriptor) {
            this.counter += 1;
            const step = {
                lin: this.counter,
                col: this.counter,
            };
            this.gameMakerService.sendStep(this.gameDescriptor.gameId, step);
        }
    }
}
