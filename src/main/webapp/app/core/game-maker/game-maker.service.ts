import { Injectable } from '@angular/core';
import { StompRService } from '@stomp/ng2-stompjs';
import * as SockJS from 'sockjs-client';
import { AuthServerProvider } from '../auth/auth-jwt.service';
import { Location } from '@angular/common';
import { Observable } from 'rxjs';
import { IMessage } from '@stomp/stompjs';
import { map } from 'rxjs/operators';
import { GameMessage, ToPlayerMessage } from 'app/core/game-maker/GameMessages';
import { IStep } from 'app/core/game-maker/IStep';

@Injectable({
    providedIn: 'root',
})
export class GameMakerService {
    constructor(private stompService: StompRService, private authServerProvider: AuthServerProvider, private location: Location) {
        //this.subscribeToUserGameChannel();
    }

    /**
     * Connection to a new game (a new game request from the current user)
     *
     * Before call this method, you have to subscribe to answers queue, using the userGameChannel property of this service:
     * gameMakerService.userGameChannel.subscribe( (msg: ToPlayerMessage) => this.onToPlayerMessage(msg) )
     *
     * Example of the message handler function:
     *
     * private onToPlayerMessage(msg: ToPlayerMessage) {
     *    switch (true) {
     *        case msg instanceof GameStatus: onGameStatus(msg as GameStatus); break;
     *        case msg instanceof YourTurn  : onYourTurn  (msg as YourTurn  ); break;
     *    }
     * }
     *
     */
    findGame() {
        this.getConnectedStompService().publish('/queue/new-game');
    }

    /**
     * A player should call this method in order to send his step to game server
     * @param gameId: number
     * @param step: IStep
     */
    sendStep(step: IStep) {
        const msg = JSON.stringify(step);
        this.getConnectedStompService().publish(`/queue/game`, msg);
    }

    private _userGameChannel: Observable<ToPlayerMessage> | null = null;
    get userGameChannel(): Observable<ToPlayerMessage> {
        if (this._userGameChannel == null) {
            this._userGameChannel = this.getConnectedStompService()
                .watch('/user/queue/game')
                .pipe(map<IMessage, ToPlayerMessage>((msg: IMessage) => GameMessage.of(msg.body).msg));
        }
        return this._userGameChannel;
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

    private getConnectedStompService(): StompRService {
        this.tryToConnect();
        return this.stompService;
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
