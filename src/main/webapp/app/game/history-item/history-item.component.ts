import { Component, Input } from '@angular/core';
import { FigureColor, FigureType, GameState } from 'app/core/game-maker/GameMessages';

@Component({
    selector: 'jhi-history-item',
    templateUrl: './history-item.component.html',
    styleUrls: ['./history-item.component.scss'],
})
export class HistoryItemComponent {
    @Input() state!: GameState;

    figureColorWhite = FigureColor.WHITE;
    figureTypeQuinn = FigureType.QUINN;

    cols = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];

    constructor() {}
}
