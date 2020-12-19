import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { CheckersSharedModule } from 'app/shared/shared.module';

import { gameState } from './game.route';
import { GamePageComponent } from './game-page/game-page.component';
import { GameBoardComponent } from './game-board/game-board.component';
import { GameBoardUserPanelComponent } from './game-board-user-panel/game-board-user-panel.component';
import { HistoryItemComponent } from './history-item/history-item.component';

@NgModule({
    imports: [CheckersSharedModule, RouterModule.forChild(gameState)],
    declarations: [GamePageComponent, GameBoardComponent, GameBoardUserPanelComponent, HistoryItemComponent],
})
export class GameModule {}
