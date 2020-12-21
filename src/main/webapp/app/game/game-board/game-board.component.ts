import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { colorOfPlayerByIndex, Figure, FigureColor, FiguresByPlayers, FigureType, P, PlayerIndex } from 'app/core/game-maker/GameStateData';

@Component({
    selector: 'jhi-game-board',
    templateUrl: './game-board.component.html',
    styleUrls: ['./game-board.component.scss'],
})
export class GameBoardComponent implements OnInit, OnChanges {
    @Input() figures: FiguresByPlayers = {};
    @Input() myPlayerIndex!: number;
    @Input() activePlayerIndex!: number;

    isMyTurn: boolean = false;

    @Output() doMove: EventEmitter<P[]> = new EventEmitter<P[]>();

    myColor: FigureColor = FigureColor.WHITE;
    FigureColor_WHITE = FigureColor.WHITE;
    FigureColor_BLACK = FigureColor.BLACK;
    figureType_QUINN = FigureType.QUINN;

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

    activeFigure: Figure | null = null;

    cellsQueue: P[] = [];

    itemStartPoint: number = 0;
    itemCellSize: number = 0;

    fx0 = 0;
    fy0 = 0;
    fxCellSize = 0;
    fyCellSize = 0;

    public colChar: string[] = [];
    public linChar: string[] = [];

    constructor() {}

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.myPlayerIndex) {
            this.myColor = colorOfPlayerByIndex(changes.myPlayerIndex.currentValue);
            if (this.myColor === FigureColor.WHITE) {
                this.colChar = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
                this.linChar = ['8', '7', '6', '5', '4', '3', '2', '1'];
                this.fx0 = this.startPoint + this.cellCenter;
                this.fy0 = this.startPoint + this.cellSize * 8 - this.cellCenter;
                this.fxCellSize = this.cellSize;
                this.fyCellSize = -this.cellSize;
            } else {
                this.colChar = ['H', 'G', 'F', 'E', 'D', 'C', 'B', 'A'];
                this.linChar = ['1', '2', '3', '4', '5', '6', '7', '8'];
                this.fx0 = this.startPoint + this.cellSize * 8 - this.cellCenter;
                this.fy0 = this.startPoint + this.cellCenter;
                this.fxCellSize = -this.cellSize;
                this.fyCellSize = this.cellSize;
            }
            this.checkIfMyTurn(changes.myPlayerIndex.currentValue, this.activePlayerIndex);
        } else if (changes.activePlayerIndex) {
            this.checkIfMyTurn(this.myPlayerIndex, changes.activePlayerIndex.currentValue);
        }
    }

    private checkIfMyTurn(myPlayerIndex: number, activePlayerIndex: number): void {
        //this.isMyTurn = myPlayerIndex === activePlayerIndex;
        this.isMyTurn = true
    }

    ngOnInit(): void {}

    clickToShaka(f: Figure, figurePlayerIndex: PlayerIndex): void {
        if (!this.isMyTurn) return; // It's my turn
        if (figurePlayerIndex !== this.myPlayerIndex) return; // It's my figure
        if (this.cellsQueue.length >= 2) return; // I didn't move another figure yet

        this.cellsQueue.length = 0;
        this.cellsQueue.push(f.p);
        this.activeFigure = f;
    }

    cellClick(l: number, c: number): void {
        if (!this.isMyTurn) return;
        this.handleCellClick(this.convertCoordinatesFromScreenToGameAxis(l, c));
    }

    activeCellClick(p: P): void {
        if (!this.isMyTurn) return;
        this.handleCellClick(p);
    }

    private handleCellClick(p: P): void {
        if (!this.isMyTurn) return;
        if (!p.isDark()) return;
        if (this.activeFigure == null) return;

        let last: P | null = null;
        if (this.cellsQueue.length > 0) last = this.cellsQueue[this.cellsQueue.length - 1];

        if (last && p.l === last.l && p.c === last.c) {
            this.doMove.emit(this.cellsQueue);
            this.eraseState();
        } else {
            this.cellsQueue.push(p);
        }
    }

    private convertCoordinatesFromScreenToGameAxis(l: number, c: number): P {
        if (this.myColor === FigureColor.WHITE) {
            return new P(7 - l, c);
        } else {
            return new P(l, 7 - c);
        }
    }

    private eraseState(): void {
        this.cellsQueue.length = 0;
        this.activeFigure = null;
    }
}
