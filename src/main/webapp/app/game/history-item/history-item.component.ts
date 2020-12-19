import { Component, Input } from '@angular/core';
import { BoardHistoryItem, FigureColor, FigureType } from 'app/core/game-maker/GameStateData';

@Component({
    selector: 'jhi-history-item',
    templateUrl: './history-item.component.html',
    styleUrls: ['./history-item.component.scss'],
})
export class HistoryItemComponent {
    @Input() historyItem!: BoardHistoryItem;

    figureColorWhite = FigureColor.WHITE;
    figureTypeQuinn = FigureType.QUINN;

    cols = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];

    constructor() {}
}
