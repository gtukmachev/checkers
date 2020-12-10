import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { AllFiguresOnBoard, FigureColor, FigureOnBoard, P } from 'app/core/game-maker/GameMessages';

@Component({
    selector: 'jhi-game-board',
    templateUrl: './game-board.component.html',
    styleUrls: ['./game-board.component.scss'],
})
export class GameBoardComponent implements OnInit {
    @Input() figures: AllFiguresOnBoard = new Map();
    @Input() myColor?: FigureColor | null = null;
    @Output() doMove: EventEmitter<P[]> = new EventEmitter<P[]>();

    FigureColor_WHITE = FigureColor.WHITE;
    FigureColor_BLACK = FigureColor.BLACK;

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

    activeShashka: FigureOnBoard | null = null;

    cellsQueue: P[] = [];

    itemStartPoint: number = 0;
    itemCellSize: number = 0;

    public colChar: string[] = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
    public linChar: string[] = [];

    constructor() {}

    ngOnInit(): void {
        if (this.myColor === FigureColor.WHITE) {
            this.colChar = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
            this.linChar = ['8', '7', '6', '5', '4', '3', '2', '1'];
            this.itemStartPoint = this.startPoint + this.cellSize * 9 - this.cellCenter;
            this.itemCellSize = -this.cellSize;
        } else {
            this.colChar = ['H', 'G', 'F', 'E', 'D', 'C', 'B', 'A'];
            this.linChar = ['1', '2', '3', '4', '5', '6', '7', '8'];
            this.itemStartPoint = this.startPoint + this.cellCenter;
            this.itemCellSize = this.cellSize;
        }
    }

    clickToShaka(f: FigureOnBoard): void {
        if (this.cellsQueue.length < 2) {
            if (this.activeShashka) {
                this.activeShashka.isActive = false;
            }
            this.cellsQueue.length = 0;
            this.cellsQueue.push({ l: f.l, c: f.c });
            this.activeShashka = f;
            f.isActive = true;
        }
    }

    cellClick(l: number, c: number): void {
        if (!this.activeShashka) return;

        let last: P | null = null;
        if (this.cellsQueue.length > 0) last = this.cellsQueue[this.cellsQueue.length - 1];

        if (last && l === last.l && c === last.c) {
            this.doMove.emit(this.cellsQueue);
            this.eraseState();
        } else {
            this.cellsQueue.push({ l: l, c: c });
        }
    }

    private eraseState(): void {
        this.cellsQueue.length = 0;
        if (this.activeShashka) {
            this.activeShashka.isActive = false;
            this.activeShashka = null;
        }
    }
}
