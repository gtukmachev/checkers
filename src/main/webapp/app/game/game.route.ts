import { Routes } from '@angular/router';

import { gamePageRoute } from './game-page/game-page.route';

const GAME_ROUTES = [gamePageRoute];

export const gameState: Routes = [
  {
    path: '',
    children: GAME_ROUTES,
  },
];
