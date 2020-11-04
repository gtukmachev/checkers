import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { CheckersSharedModule } from 'app/shared/shared.module';

import { gameState } from './game.route';
import { GamePageComponent } from './game-page/game-page.component';

@NgModule({
  imports: [CheckersSharedModule, RouterModule.forChild(gameState)],
  declarations: [GamePageComponent],
})
export class GameModule {}
