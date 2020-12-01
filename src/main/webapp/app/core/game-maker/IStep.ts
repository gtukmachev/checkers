import { P } from 'app/core/game-maker/GameMessages';

export interface IStep {
    nTurn: number;
    cellsQueue: P[];
}
