export class ClassCastException {
    public readonly message: string;

    constructor(message: string) {
        this.message = message;
    }
}

export enum FigureType {
    STONE = 'STONE',
    QUINN = 'QUINN',
    EMPTY = '',
}

export enum FigureColor {
    BLACK = 'BLACK',
    WHITE = 'WHITE',
    EMPTY = '',
}

export enum MoveStatus {
    OK = 'OK',
    EMPTY = '',
    ERROR = 'ERROR',
}

export interface GameMessage {
    readonly gameId: number;
    readonly msgType: string;
    readonly msg: GameInfo | GameStatus | ItIsNotYourStepError | WaitingForAGame;
}

export interface Figure {
    readonly type: FigureType;
    readonly color: FigureColor;
}

export const NO_FIGURE: Figure = { type: FigureType.EMPTY, color: FigureColor.EMPTY };

export interface FigureOnBoard {
    l: number;
    c: number;
    isQuinn: boolean;
    isActive: boolean;
}
export type AllFiguresOnBoard = Map<FigureColor, FigureOnBoard[]>;

export interface ItIsNotYourStepError {}

export interface WaitingForAGame {}

export interface GameInfo {
    readonly gameId: number;
    readonly you: PlayerInfo;
    readonly players: PlayerInfo[];
    readonly gameStatus: GameStatus;
}

export interface GameStatus {
    readonly currentState: GameState;
    readonly history: GameState[];
}

export interface PlayerInfo {
    readonly name: string;
    readonly index: number;
    readonly color: FigureColor;
}

export interface Move {
    readonly player: number;
    readonly figure: Figure;
    readonly steps: Step[];
    readonly status: MoveStatus;
}

export const NO_MOVE: Move = { player: -1, steps: [], figure: NO_FIGURE, status: MoveStatus.EMPTY };

export interface GameState {
    readonly turn: number;
    readonly activePlayer: number;
    readonly lastMove: Move;
    readonly field: Field;
}

export interface Field {
    desk: (Figure | null)[];
}

export const b: Figure = { type: FigureType.STONE, color: FigureColor.BLACK };
export const w: Figure = { type: FigureType.STONE, color: FigureColor.WHITE };
export const bq: Figure = { type: FigureType.QUINN, color: FigureColor.BLACK };
export const wq: Figure = { type: FigureType.QUINN, color: FigureColor.WHITE };
export const o: Figure | null = null;

export function initialGameState(): GameState {
    return {
        turn: 0,
        activePlayer: 0,
        lastMove: NO_MOVE,
        field: {
            desk: [
                // A  B  C  D  E  F  G  H
                w,
                o,
                w,
                o,
                w,
                o,
                w,
                o, // 1
                o,
                w,
                o,
                w,
                o,
                w,
                o,
                w, // 2
                w,
                o,
                w,
                o,
                w,
                o,
                w,
                o, // 3
                o,
                o,
                o,
                o,
                o,
                o,
                o,
                o, // 4
                o,
                o,
                o,
                o,
                o,
                o,
                o,
                o, // 5
                o,
                b,
                o,
                b,
                o,
                b,
                o,
                b, // 6
                b,
                o,
                b,
                o,
                b,
                o,
                b,
                o, // 7
                o,
                b,
                o,
                b,
                o,
                b,
                o,
                b, // 8
            ],
        },
    };
}

export interface Step {
    readonly start: P;
    readonly end: P;
    readonly shot?: P | null;
    readonly shotFigure?: Figure | null;
}

export interface P {
    readonly l: number;
    readonly c: number;
}
