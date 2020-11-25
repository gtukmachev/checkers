import { Component, OnDestroy, OnInit } from '@angular/core';
import { GameMakerService } from 'app/core/game-maker/game-maker.service';
import { ToPlayerMessage } from 'app/core/game-maker/GameMessages';
import { Subscription } from 'rxjs';

@Component({
    selector: 'jhi-game-page',
    templateUrl: './game-page.component.html',
    styleUrls: ['./game-page.component.scss'],
})
export class GamePageComponent implements OnInit, OnDestroy {
    incomeMessages: ToPlayerMessage[] = [];
    counter: number = 0;

    private gameSubscription?: Subscription;

    constructor(private gameMakerService: GameMakerService) {}

    ngOnInit(): void {
        this.gameSubscription = this.gameMakerService.userGameChannel.subscribe((msg: ToPlayerMessage) => this.onToPlayerMessage(msg));
    }

    ngOnDestroy(): void {
        this.gameSubscription?.unsubscribe();
    }

    private onToPlayerMessage(msg: ToPlayerMessage) {
        this.incomeMessages.push(msg);

        // switch (true) {
        //     case msg instanceof GameStatus: onGameStatus(msg as GameStatus); break;
        //     case msg instanceof YourStep  : onYourTurn  (msg as YourTurn  ); break;
        // }
    }

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
}
