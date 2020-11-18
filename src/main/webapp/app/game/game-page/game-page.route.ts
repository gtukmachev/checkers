import { Route } from '@angular/router';
import { GamePageComponent } from './game-page.component';

export const gamePageRoute: Route = {
    path: '',
    component: GamePageComponent,
    data: {
        authorities: [],
        pageTitle: 'game-page.title',
    },
};
