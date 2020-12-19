import { Component, Input, OnInit } from '@angular/core';
import { PlayerInfo } from 'app/core/game-maker/ExternalMessages';

@Component({
    selector: 'jhi-game-board-user-panel',
    templateUrl: './game-board-user-panel.component.html',
    styleUrls: ['./game-board-user-panel.component.scss'],
})
export class GameBoardUserPanelComponent implements OnInit {
    @Input() player!: PlayerInfo;
    @Input() activePlayerIndex!: number;

    constructor() {}

    ngOnInit(): void {}
}
