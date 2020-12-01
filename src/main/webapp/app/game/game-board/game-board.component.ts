import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Figure, P } from 'app/core/game-maker/GameMessages';

@Component({
    selector: 'jhi-game-board',
    templateUrl: './game-board.component.html',
    styleUrls: ['./game-board.component.scss'],
    //    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameBoardComponent implements OnInit {
    @Input() field: (Figure | null)[][] = [];
    @Output() doMove: EventEmitter<P[]> = new EventEmitter<P[]>();

    // parameters of board graphic
    cellBorderSize = 7;
    cellSize = 101; // [0..101]

    cellOffset = (this.cellBorderSize - 1) / 2;
    cellInnerSize = this.cellSize - this.cellOffset * 2;

    cellCenter = (this.cellSize - 1) / 2; // [0..(50)..101]

    borderSize = this.cellCenter; // border around the field = half of 1 cell size
    borderInnerSize = 10;
    totalSize = this.cellSize * 8 + this.borderSize * 2;
    startPoint = this.borderSize;

    vb = `0 0 ${this.totalSize} ${this.totalSize}`;

    // the board sate
    static emptyActivationMatrix: boolean[][] = [
        [false, false, false, false, false, false, false, false],
        [false, false, false, false, false, false, false, false],
        [false, false, false, false, false, false, false, false],
        [false, false, false, false, false, false, false, false],
        [false, false, false, false, false, false, false, false],
        [false, false, false, false, false, false, false, false],
        [false, false, false, false, false, false, false, false],
        [false, false, false, false, false, false, false, false],
    ];

    activeCells: boolean[][] = GameBoardComponent.emptyActivationMatrix;
    cellsQueue: P[] = [];

    public colChar: string[] = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];

    constructor() {}

    ngOnInit(): void {}

    cellClick(l: number, c: number) {
        if (!this.activeCells[l][c]) {
            this.cellsQueue.push({ l: l, c: c });
            this.activeCells[l][c] = true;
        } else {
            if (this.cellsQueue.length > 1) {
                this.doMove.emit(this.cellsQueue);
            }
            this.eraseState();
        }
    }

    private eraseState() {
        this.cellsQueue.forEach((p: P) => {
            this.activeCells[p.l][p.c] = false;
        });
        this.cellsQueue.length = 0;
    }
}
