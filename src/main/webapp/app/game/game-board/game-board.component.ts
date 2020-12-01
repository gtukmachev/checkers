import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { Figure } from 'app/core/game-maker/GameMessages';

@Component({
    selector: 'jhi-game-board',
    templateUrl: './game-board.component.html',
    styleUrls: ['./game-board.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameBoardComponent implements OnInit {
    @Input() field: (Figure | null)[][] = [];

    cellSize = 101; // [0..101]
    cellCenter = (this.cellSize - 1) / 2; // [0..(50)..101]
    borderSize = this.cellCenter; // border around the field = half of 1 cell size
    borderInnerSize = 10;
    totalSize = this.cellSize * 8 + this.borderSize * 2;
    startPoint = this.borderSize;

    public colChar: string[] = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];

    constructor() {}

    ngOnInit(): void {}
}
