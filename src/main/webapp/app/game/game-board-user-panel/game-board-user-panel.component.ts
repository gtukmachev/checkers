import { Component, Input, OnInit } from '@angular/core';
import { PlayerInfo } from 'app/core/game-maker/GameMessages';

@Component({
    selector: 'jhi-game-board-user-panel',
    templateUrl: './game-board-user-panel.component.html',
    styleUrls: ['./game-board-user-panel.component.scss'],
})
export class GameBoardUserPanelComponent implements OnInit {
    @Input() player: PlayerInfo | null = null;
    @Input() activePlayerIndex: number | null = null;

    constructor() {}

    ngOnInit(): void {}
}
