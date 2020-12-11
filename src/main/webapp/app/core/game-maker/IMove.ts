import { P } from 'app/core/game-maker/GameMessages';

export interface IMove {
    nTurn: number;
    cellsQueue: P[];
}
