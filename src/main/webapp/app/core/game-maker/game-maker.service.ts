import { Injectable } from '@angular/core';
import { StompRService } from '@stomp/ng2-stompjs';
import { IFoundGameDescriptor } from './IFoundGameDescriptor';
import * as SockJS from 'sockjs-client';
import { AuthServerProvider } from '../auth/auth-jwt.service';
import { Location } from '@angular/common';
import { Subscription } from 'rxjs';
import { IMessage } from '@stomp/stompjs';

type onGameFoundHandler = (gameDescriptor: IFoundGameDescriptor) => void;

@Injectable({
  providedIn: 'root',
})
export class GameMakerService {
  private gameWaitingSubscription: Subscription | null = null;

  constructor(private stompService: StompRService, private authServerProvider: AuthServerProvider, private location: Location) {}

  findGame(onGameFound: onGameFoundHandler) {
    this.tryToConnect();
    /*
    onGameFound({
      gameId: 1,
      color: "white",
      partnerName: "Bot"
    });
*/
    this.gameWaitingSubscription = this.stompService
      .watch('/user/queue/new-game-request')
      .subscribe((msg: IMessage) => this.onGameFound(msg, onGameFound));
  }

  private onGameFound(msg: IMessage, onGameFound: onGameFoundHandler) {
    let foundGameDescriptor = JSON.parse(msg.body) as IFoundGameDescriptor;
    if (this.gameWaitingSubscription) this.gameWaitingSubscription.unsubscribe();
    onGameFound(foundGameDescriptor);
  }

  private buildWebSocketUrl(): string {
    let url = '/websocket/tracker';
    url = this.location.prepareExternalUrl(url);
    const authToken = this.authServerProvider.getToken();
    if (authToken) {
      url += '?access_token=' + authToken;
    }

    return url;
  }

  private tryToConnect() {
    if (this.stompService.active) return;

    let url: string = this.buildWebSocketUrl();

    this.stompService.config = {
      url: () => new SockJS(url),
      headers: {},
      heartbeat_in: 0, // (Typical value 0 - disabled)
      heartbeat_out: 20000, // Typical value 20000 - every 20 seconds
      reconnect_delay: 5000, // 5 seconds
      debug: true,
    };

    this.stompService.initAndConnect();
  }
}
