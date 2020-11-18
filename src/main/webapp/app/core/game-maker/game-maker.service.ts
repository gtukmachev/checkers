import { Injectable } from '@angular/core';
import { StompRService } from '@stomp/ng2-stompjs';
import { IFoundGameDescriptor } from './IFoundGameDescriptor';
import * as SockJS from 'sockjs-client';
import { AuthServerProvider } from '../auth/auth-jwt.service';
import { Location } from '@angular/common';
import { Observable, Subscription } from 'rxjs';
import { IMessage } from '@stomp/stompjs';
import { first, map } from 'rxjs/operators';
import { IGameMessage } from 'app/core/game-maker/IGameMessage';
import { IStep } from 'app/core/game-maker/IStep';

type OnGameFoundHandler = (gameDescriptor: IFoundGameDescriptor) => void;
type OnGameMessageHandler = (gameMessage: IGameMessage) => void;

@Injectable({
    providedIn: 'root',
})
export class GameMakerService {
    constructor(private stompService: StompRService, private authServerProvider: AuthServerProvider, private location: Location) {}

    private gameDescriptor: IFoundGameDescriptor | null = null;
    private gameChannelSubscription: Subscription | null = null;

    /**
     * Connection to a new game
     *
     * @param onGameFound - callback. will be invoked only once - on connection to a game.
     * @param onGameMessage - callback. Will be called on each message from the game server, such a new step request.
     */
    findGame(onGameFound: OnGameFoundHandler, onGameMessage: OnGameMessageHandler) {
        this.getConnectedStompService()
            .watch('/user/queue/new-game-request')
            .pipe(first())
            .subscribe((msg: IMessage) => this.gameIsReady(msg, onGameFound, onGameMessage));
    }

    /**
     * A player should call this method in order to send his step to game server
     * @param gameId: number
     * @param step: IStep
     */
    sendStep(gameId: number, step: IStep) {
        const msg = JSON.stringify(step);
        this.getConnectedStompService().publish(`/queue/game/${gameId}/step`, msg);
    }

    unsubscribeFromGameIfAny(game: IFoundGameDescriptor) {
        if (game && this.gameChannelSubscription) {
            this.gameChannelSubscription.unsubscribe();
            this.gameChannelSubscription = null;
        }
    }

    private gameIsReady(msg: IMessage, onGameFound: OnGameFoundHandler, onGameMessage: OnGameMessageHandler) {
        this.gameDescriptor = JSON.parse(msg.body) as IFoundGameDescriptor;

        onGameFound(this.gameDescriptor);

        this.gameChannelSubscription = this.getGameUserChannel(this.gameDescriptor.gameId).subscribe(onGameMessage);
    }

    private getGameUserChannel(gameId: number): Observable<IGameMessage> {
        return this.getConnectedStompService()
            .watch(`/user/queue/game/${gameId}`)
            .pipe(map<IMessage, IGameMessage>((msg: IMessage) => JSON.parse(msg.body) as IGameMessage));
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
