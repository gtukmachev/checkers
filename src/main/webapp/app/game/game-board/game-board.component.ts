import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Figure, FigureColor, FigureType, P } from 'app/core/game-maker/GameMessages';

interface F {
    l: number;
    c: number;
    figure: Figure;
}

@Component({
    selector: 'jhi-game-board',
    templateUrl: './game-board.component.html',
    styleUrls: ['./game-board.component.scss'],
    //    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameBoardComponent implements OnInit {
    @Input() field: (Figure | null)[][] = [];
    @Output() doMove: EventEmitter<P[]> = new EventEmitter<P[]>();

    FigureType_EMPTY = FigureType.EMPTY;
    FigureColor_WHITE = FigureColor.WHITE;
    FigureColor_BLACK = FigureColor.BLACK;

    figures: F[] = [
        { l: 0, c: 1, figure: { type: FigureType.STONE, color: FigureColor.BLACK } },
        { l: 0, c: 3, figure: { type: FigureType.STONE, color: FigureColor.BLACK } },
        { l: 0, c: 5, figure: { type: FigureType.STONE, color: FigureColor.BLACK } },
        { l: 0, c: 7, figure: { type: FigureType.STONE, color: FigureColor.BLACK } },
        { l: 1, c: 0, figure: { type: FigureType.STONE, color: FigureColor.BLACK } },
        { l: 1, c: 2, figure: { type: FigureType.STONE, color: FigureColor.BLACK } },
        { l: 1, c: 4, figure: { type: FigureType.STONE, color: FigureColor.BLACK } },
        { l: 1, c: 6, figure: { type: FigureType.STONE, color: FigureColor.BLACK } },
        { l: 2, c: 1, figure: { type: FigureType.STONE, color: FigureColor.BLACK } },
        { l: 2, c: 3, figure: { type: FigureType.STONE, color: FigureColor.BLACK } },
        { l: 2, c: 5, figure: { type: FigureType.STONE, color: FigureColor.BLACK } },
        { l: 2, c: 7, figure: { type: FigureType.STONE, color: FigureColor.BLACK } },

        { l: 7, c: 0, figure: { type: FigureType.STONE, color: FigureColor.WHITE } },
        { l: 7, c: 2, figure: { type: FigureType.STONE, color: FigureColor.WHITE } },
        { l: 7, c: 4, figure: { type: FigureType.STONE, color: FigureColor.WHITE } },
        { l: 7, c: 6, figure: { type: FigureType.STONE, color: FigureColor.WHITE } },
        { l: 6, c: 1, figure: { type: FigureType.STONE, color: FigureColor.WHITE } },
        { l: 6, c: 3, figure: { type: FigureType.STONE, color: FigureColor.WHITE } },
        { l: 6, c: 5, figure: { type: FigureType.STONE, color: FigureColor.WHITE } },
        { l: 6, c: 7, figure: { type: FigureType.STONE, color: FigureColor.WHITE } },
        { l: 5, c: 0, figure: { type: FigureType.STONE, color: FigureColor.WHITE } },
        { l: 5, c: 2, figure: { type: FigureType.STONE, color: FigureColor.WHITE } },
        { l: 5, c: 4, figure: { type: FigureType.STONE, color: FigureColor.WHITE } },
        { l: 5, c: 6, figure: { type: FigureType.STONE, color: FigureColor.WHITE } },
    ];

    // parameters of board graphic
    cellSize = 100; // size of board field
    startPoint = this.cellSize; // offset of the first cell

    totalSize = this.cellSize * 8 + this.startPoint * 2; // total board size
    cellCenter = this.cellSize / 2; // offset of cell center

    stoneRadius = 40;

    borderLine = 4;
    borderWidth = 15;
    cornerSize = this.startPoint / 3;

    b1 = this.borderLine / 2;
    b1s = this.totalSize - this.borderLine;
    b2 = this.borderWidth;
    b2s = this.totalSize - this.borderWidth * 2;

    b3 = this.startPoint - this.borderWidth;
    b3s = this.cellSize * 8 + this.borderWidth * 2;
    b4 = this.startPoint - this.borderLine / 2;
    b4s = this.cellSize * 8 + this.borderLine;

    con00 = this.b1;
    con01 = this.b2;
    con10 = this.totalSize - this.cornerSize - this.borderLine / 2;
    con11 = this.totalSize - this.cornerSize - this.borderWidth;

    vb = `0 0 ${this.totalSize} ${this.totalSize}`;

    boardIndex = [0, 1, 2, 3, 4, 5, 6, 7];

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
