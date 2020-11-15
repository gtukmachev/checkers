import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import './vendor';
import { CheckersSharedModule } from 'app/shared/shared.module';
import { CheckersCoreModule } from 'app/core/core.module';
import { CheckersAppRoutingModule } from './app-routing.module';
import { CheckersHomeModule } from './home/home.module';
import { CheckersEntityModule } from './entities/entity.module';
import { MainComponent } from './layouts/main/main.component';
import { NavbarComponent } from './layouts/navbar/navbar.component';
import { FooterComponent } from './layouts/footer/footer.component';
import { PageRibbonComponent } from './layouts/profiles/page-ribbon.component';
import { ActiveMenuDirective } from './layouts/navbar/active-menu.directive';
import { ErrorComponent } from './layouts/error/error.component';
import { GameModule } from 'app/game/game.module';
import { StompRService } from '@stomp/ng2-stompjs';

@NgModule({
  imports: [
    BrowserModule,
    CheckersSharedModule,
    CheckersCoreModule,
    CheckersHomeModule,
    // jhipster-needle-angular-add-module JHipster will add new module here
    CheckersEntityModule,
    CheckersAppRoutingModule,
    GameModule,
  ],
  declarations: [MainComponent, NavbarComponent, ErrorComponent, PageRibbonComponent, ActiveMenuDirective, FooterComponent],
  providers: [StompRService],
  bootstrap: [MainComponent],
})
export class CheckersAppModule {}
