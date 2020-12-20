import { ClassCastException, RuntimeException, Throwable } from './exceptions';
import { getFromJson } from 'app/core/game-maker/json-utils';

export abstract class GameError extends Throwable {
    protected constructor(message: string, cause: Throwable | null) {
        super(message, cause);
    }
}

export class OutOfBoardError extends GameError {
    constructor(public readonly p: P) {
        super(`The point you are working with is out of the board: ${p.toHumanCoordinates()}`, null);
    }
}

export enum FigureType {
    STONE = 'STONE',
    QUINN = 'QUINN',
}
export function fromJson_FigureType(json: any): FigureType {
    switch (json) {
        case 'STONE':
            return FigureType.STONE;
        case 'QUINN':
            return FigureType.QUINN;
        default:
            throw new ClassCastException(`The value cannot be converted to enum FigureType: "${json}"`, json);
    }
}

export type PlayerIndex = number;

export enum FigureColor {
    WHITE = 'WHITE',
    BLACK = 'BLACK',
    BLUE = 'BLUE',
    GREEN = 'GREEN',
    CYAN = 'CYAN',
}
export function colorOfPlayerByIndex(index: PlayerIndex): FigureColor {
    if (index === 0) return FigureColor.WHITE;
    if (index === 1) return FigureColor.BLACK;
    if (index === 2) return FigureColor.BLUE;
    if (index === 3) return FigureColor.GREEN;
    if (index === 4) return FigureColor.CYAN;
    throw 'Only 5 players are supported';
}

export type GameHistory = BoardHistoryItem[];

export interface FiguresByPlayers {
    [key: number]: Figure[]; // PlayerIndex -> list og Figures
}

type DeskFigureOpt = DeskFigure | null;

/**
 * Coordinates **(Ponumber)** on the game board
 *
 * 1. `l` = line
 * 2. `c` = column
 */
export class P {
    private static chars: string[] = [
        'A',
        'B',
        'C',
        'D',
        'E',
        'F',
        'G',
        'H',
        'I',
        'J',
        'K',
        'L',
        'M',
        'N',
        'O',
        'P',
        'Q',
        'R',
        'S',
        'T',
        'U',
        'V',
        'X',
        'W',
        'Y',
        'Z',
    ];

    constructor(public readonly l: number, public readonly c: number) {}

    public static fromIndex(plainBoardIndex: number, boardLines: number, boardColumns: number): P {
        return new P(plainBoardIndex / boardLines, plainBoardIndex % boardColumns);
    }

    public static fromJson(json: any): P {
        let l_: number = getFromJson<number>(json, 'l');
        let c_: number = getFromJson<number>(json, 'c');
        return new P(l_, c_);
    }

    public toHumanCoordinates(): string {
        return `${this.l + 1}${P.chars[this.c]}`;
    }

    public isDark(): boolean {
        return (this.l + this.c) % 2 === 0;
    }
}

export class Figure {
    constructor(public readonly id: number, public readonly p: P, public readonly type: FigureType) {}

    public static fromJson(json: any): Figure {
        let id_: number = getFromJson<number>(json, 'id');
        let p_: P = P.fromJson(getFromJson<any>(json, 'p'));
        let type_: FigureType = fromJson_FigureType(getFromJson<any>(json, 'type'));
        return new Figure(id_, p_, type_);
    }
}

function fromJson_figuresByPlayers(json: object): FiguresByPlayers {
    const figuresByPlayers: FiguresByPlayers = {};
    for (const key in json) {
        if (Object.prototype.hasOwnProperty.call(json, key)) {
            const keyN: number = +key;
            if (isNaN(keyN)) {
                throw new RuntimeException(
                    `FiguresByPlayers parsing errors: keys should have type [Integer], but found the value: '${key}'`
                );
            }

            const arr = json[key];
            if (!(arr instanceof Array)) {
                throw new RuntimeException(`FiguresByPlayers should contains collections, but found the value: '${arr}'`);
            }

            const figures: Figure[] = arr.map(arrItem => Figure.fromJson(arrItem));
            figuresByPlayers[keyN] = figures;
        }
    }
    return figuresByPlayers;
}

export class Board {
    constructor(public readonly turn: number, public readonly activePlayerIndex: PlayerIndex, public readonly figures: FiguresByPlayers) {}

    static initialBoard(desk: Desk): Board {
        return new Board(0, 0, desk.mapToFiguresByPlayers());
    }

    public static fromJson(json: any): Board {
        let turn_: number = getFromJson<number>(json, 'turn');
        let activePlayerIndex_: PlayerIndex = getFromJson<number>(json, 'activePlayerIndex');
        let figures_: FiguresByPlayers = fromJson_figuresByPlayers(getFromJson<any>(json, 'figures'));
        return new Board(turn_, activePlayerIndex_, figures_);
    }
}

export class DeskFigure {
    constructor(public readonly player: PlayerIndex, public readonly type: FigureType, public readonly id: number) {}

    public static fromJson(json: any): DeskFigure {
        let player_: PlayerIndex = getFromJson<number>(json, 'player');
        let type_: FigureType = fromJson_FigureType(getFromJson<any>(json, 'type'));
        let id_: number = getFromJson<number>(json, 'id');
        return new DeskFigure(player_, type_, id_);
    }
}

export class Desk {
    constructor(public readonly lines: number, public readonly columns: number, public readonly figures: DeskFigureOpt[]) {}

    static initialDesk(lines: number, columns: number, players: number): Desk {
        let arr: DeskFigureOpt[] = new Array<DeskFigureOpt>(lines * columns);

        if (players < 1 || players > 2) throw new RuntimeException('Only 2 players games are supported now!', null);

        let desk = new Desk(lines, columns, arr);

        let nextFigureId = 0;

        // 3 first of WHITE in 2x2 game
        for (let l = 0; l < 3; l++)
            for (let c = 0; c < columns; c++)
                if ((l + c) % 2 === 0) {
                    desk.setF(l, c, new DeskFigure(0, FigureType.STONE, nextFigureId++));
                }

        // 3 last rows of BLACK in 2x2 game
        for (let l = lines - 3; l < lines; l++)
            for (let c = 0; c < columns; c++)
                if ((l + c) % 2 === 0) {
                    desk.setF(l, c, new DeskFigure(1, FigureType.STONE, nextFigureId++));
                }
        return desk;
    }

    setF(l: number, c: number, value: DeskFigureOpt): void {
        if (!this.isOnDesk(l, c)) throw new OutOfBoardError(new P(l, c));
        this.figures[l * this.lines + c] = value;
    }
    getF(l: number, c: number): DeskFigureOpt {
        if (!this.isOnDesk(l, c)) throw new OutOfBoardError(new P(l, c));
        return this.figures[l * this.lines + c];
    }

    setFp(p: P, value: DeskFigureOpt): void {
        this.setF(p.l, p.c, value);
    }
    getFp(p: P): DeskFigureOpt {
        return this.getF(p.l, p.c);
    }

    isOnDesk(l: number, c: number): boolean {
        return l >= 0 && l < this.lines && c >= 0 && c < this.columns;
    }
    isOnDeskP(p: P): boolean {
        return this.isOnDesk(p.l, p.c);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    mapToFiguresByPlayers(): FiguresByPlayers {
        let figuresByPlayers: FiguresByPlayers = {};

        for (let i = 0; i < this.figures.length; i++) {
            let f: DeskFigureOpt = this.figures[i];
            if (f != null) {
                let playerFigures: Figure[] = figuresByPlayers[f.player];
                if (playerFigures == null) {
                    playerFigures = [];
                    figuresByPlayers[f.player] = playerFigures;
                }

                playerFigures.push(new Figure(f.id, P.fromIndex(i, this.lines, this.columns), f.type));
            }
        }

        return figuresByPlayers;
    }

    figureAt(l: number, c: number): Figure | null {
        const deskFigure = this.getF(l, c);
        if (deskFigure == null) return null;
        return new Figure(deskFigure.id, new P(l, c), deskFigure.type);
    }
    figureAtP(p: P): Figure | null {
        return this.figureAt(p.l, p.c);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    equals(other: any): Boolean {
        if (this === other) return true;
        if (!(other instanceof Desk)) return false;

        if (other.columns !== this.columns) return false;
        if (other.lines !== this.lines) return false;

        if (other.figures === this.figures) return true;

        if (other.figures == null || this.figures == null) return false;
        if (other.figures.length !== this.figures.length) return false;

        for (let i = 0; i < this.figures.length; ++i) {
            if (this.figures[i] !== other.figures[i]) return false;
        }

        return true;
    }
}

export class FigureStep {
    constructor(public readonly begin: P, public readonly end: P, public readonly shot: Figure | null) {}

    public static fromJson(json: any): FigureStep {
        let begin_: P = P.fromJson(getFromJson<any>(json, 'begin'));
        let end_: P = P.fromJson(getFromJson<any>(json, 'end'));
        let shotObj = getFromJson<any | null>(json, 'shot');
        let shot_: Figure | null = shotObj == null ? null : Figure.fromJson(shotObj);
        return new FigureStep(begin_, end_, shot_);
    }
}

export class PlayerMove {
    constructor(
        public readonly playerIndex: PlayerIndex,
        public readonly figure: Figure,
        public readonly steps: FigureStep[],
        public readonly err: String | null
    ) {}

    public static fromJson(json: any): PlayerMove {
        let playerIndex_: PlayerIndex = getFromJson<number>(json, 'playerIndex');
        let figure_: Figure = Figure.fromJson(getFromJson<any>(json, 'figure'));
        let steps_: FigureStep[] = getFromJson<any[]>(json, 'steps').map(item => FigureStep.fromJson(item));
        let err_: String | null = getFromJson<String | null>(json, 'err');
        return new PlayerMove(playerIndex_, figure_, steps_, err_);
    }
}

export class BoardHistoryItem {
    constructor(public readonly before: Board, public readonly move: PlayerMove) {}

    public static fromJson(json: any): BoardHistoryItem {
        let before_: Board = Board.fromJson(getFromJson<any>(json, 'before'));
        let move_: PlayerMove = PlayerMove.fromJson(getFromJson<any>(json, 'move'));
        return new BoardHistoryItem(before_, move_);
    }
}
