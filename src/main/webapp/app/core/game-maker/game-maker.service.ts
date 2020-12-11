import { Injectable } from '@angular/core';
import { StompRService } from '@stomp/ng2-stompjs';
import * as SockJS from 'sockjs-client';
import { AuthServerProvider } from '../auth/auth-jwt.service';
import { Location } from '@angular/common';
import { Observable } from 'rxjs';
import { IMessage } from '@stomp/stompjs';
import { map } from 'rxjs/operators';
import { GameMessage } from 'app/core/game-maker/GameMessages';
import { IMove } from 'app/core/game-maker/IMove';

@Injectable({
    providedIn: 'root',
})
export class GameMakerService {
    private _userGameChannel: Observable<GameMessage> | null = null;

    constructor(private stompService: StompRService, private authServerProvider: AuthServerProvider, private location: Location) {
        // this.subscribeToUserGameChannel();
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
    public findGame(): void {
        this.getConnectedStompService().publish('/queue/new-game');
    }

    /**
     * A player should call this method in order to send his step to game server
     * @param step: IStep
     */
    public sendStep(step: IMove): void {
        const msg = JSON.stringify(step);
        this.getConnectedStompService().publish(`/queue/steps`, msg);
    }

    /**
     * <p>
     *     This methods - a capability to restore game state in case of any errors.<br/>
     *     Usually, this method will be never invoked, because, in case of connection
     *     lost - after the reconnection, the client will automatically restore all<br/>
     *     the subscriptions (hanks to the StompRService)  and the server will <br/>
     *     update us (it's a default react on subscription) with the current game state.
     * </p>
     * <p> But, if state is broken for some reason - we can use this function </p>
     */
    public updateGameStateRequest(): void {
        this.getConnectedStompService().publish('/queue/state');
    }

    get userGameChannel(): Observable<GameMessage> {
        if (this._userGameChannel == null) {
            this._userGameChannel = this.getConnectedStompService()
                .watch('/user/queue/game')
                .pipe(map<IMessage, GameMessage>((msg: IMessage) => JSON.parse(msg.body) as GameMessage));
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

    private tryToConnect(): void {
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
