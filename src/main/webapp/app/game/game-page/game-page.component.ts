import { Component, OnInit } from '@angular/core';
import { GameMakerService } from 'app/core/game-maker/game-maker.service';
import { IFoundGameDescriptor } from 'app/core/game-maker/IFoundGameDescriptor';

@Component({
  selector: 'jhi-game-page',
  templateUrl: './game-page.component.html',
  styleUrls: ['./game-page.component.scss'],
})
export class GamePageComponent implements OnInit {
  gameDescriptor: IFoundGameDescriptor | null = null;

  constructor(private gameMakerService: GameMakerService) {}

  ngOnInit(): void {}

  startGameRequest() {
    this.gameMakerService.findGame((gameDescriptor_: IFoundGameDescriptor) => {
      this.gameDescriptor = gameDescriptor_;
      console.log('Game found: ', this.gameDescriptor);
    });
  }
}
