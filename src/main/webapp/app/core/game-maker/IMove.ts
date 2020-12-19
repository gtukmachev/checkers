import { P } from 'app/core/game-maker/GameStateData';

export interface IMove {
    turn: number;
    cellsQueue: P[];
}
