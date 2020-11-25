export class ToPlayerMessage {
    static of(subclassShortName: string, obj: any) {
        switch (subclassShortName) {
            case 'GameStatus':
                return new GameStatus(obj.gameId, obj.players, obj.activePlayer);
            case 'YourStep':
                return new YourStep(obj.nTurn);
            case 'ItIsNotYourStepError':
                return new ItIsNotYourStepError();
            case 'WaitingForAGame':
                return new WaitingForAGame();
            default:
                throw new ClassCastException(
                    'Type of inner object is unrecognized! Supported types are: [GameStatus, YourStep, ItIsNotYourStepError, WaitingForAGame]'
                );
        }
    }
}
export class GameStatus extends ToPlayerMessage {
    constructor(readonly gameId: number, readonly players: string[], readonly activePlayer: string) {
        super();
    }
}
export class YourStep extends ToPlayerMessage {
    constructor(readonly nTurn: number) {
        super();
    }
}
export class ItIsNotYourStepError extends ToPlayerMessage {
    constructor() {
        super();
    }
}
export class WaitingForAGame extends ToPlayerMessage {
    constructor() {
        super();
    }
}

interface GameMessages {
    readonly gameId: number;
    readonly msgType: string;
    readonly msg: any;
}

export class ClassCastException {
    constructor(readonly message: string, readonly cause: any | null = null) {}
}

export class GameMessage {
    static of(jsonString: string): GameMessage {
        const o = JSON.parse(jsonString) as GameMessages;

        try {
            return new GameMessage(o.gameId, o.msgType, ToPlayerMessage.of(o.msgType, o.msg));
        } catch (er) {
            throw new ClassCastException(`Can't build a GameMessage object from the json: ${jsonString}`, er);
        }
    }

    constructor(readonly gameId: number, readonly msgType: string, readonly msg: ToPlayerMessage) {}
}
