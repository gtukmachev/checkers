export class ClassCastException {
    constructor(readonly message: string) {}
}

export interface GameMessage {
    readonly gameId: number;
    readonly msgType: string;
    readonly msg: GameInfo | GameStatus | ItIsNotYourStepError | WaitingForAGame;
}

export enum FigureType {
    STONE,
    QUINN,
}
export enum FigureColor {
    BLACK,
    WHITE,
}
export enum MoveStatus {
    EMPTY,
    OK,
    ERROR,
}

export interface Figure {
    readonly type: FigureType;
    readonly color: FigureColor;
}

const  b:Figure = {type: FigureType.STONE, color: FigureColor.BLACK}
const  w:Figure = {type: FigureType.STONE, color: FigureColor.WHITE}
const bq:Figure = {type: FigureType.QUINN, color: FigureColor.BLACK}
const wq:Figure = {type: FigureType.QUINN, color: FigureColor.WHITE}
const  o:Figure | null = null


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

export interface GameState {
    readonly nTurn: number;
    readonly activePlayer: number;
    readonly lastMove: Move | null;
    readonly field: (Figure | null)[][];
}

export function initialGameState(): GameState {
    return {
        nTurn: 0,
        activePlayer: 0,
        lastMove: null,
        field: [[w, o, w, o, w, o, w, o],
                [o, w, o, w, o, w, o, w],
                [w, o, w, o, w, o, w, o],
                [o, o, o, o, o, o, o, o],
                [o, o, o, o, o, o, o, o],
                [o, b, o, b, o, b, o, b],
                [b, o, b, o, b, o, b, o],
                [o, b, o, b, o, b, o, b]]
    }
}


export interface Move {
    readonly player: number;
    readonly figure: Figure;
    readonly steps: Step[];
    readonly status: MoveStatus;
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
