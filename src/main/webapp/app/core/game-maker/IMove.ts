import { P } from 'app/core/game-maker/GameMessages';

export interface IMove {
    turn: number;
    cellsQueue: P[];
}
