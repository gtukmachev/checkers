import { Board, BoardHistoryItem, colorOfPlayerByIndex, FigureColor, GameHistory, P, PlayerMove } from './GameStateData';
import { ClassCastException, RuntimeException } from 'app/core/game-maker/exceptions';
import { getFromJson } from './json-utils';

export class GameMessageParsingError extends RuntimeException {
    constructor(public readonly json: any, cause: any) {
        super('json parsing error: ' + JSON.stringify(json), cause);
    }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
export class ToPlayerMessage {}

export class WaitingForAGame extends ToPlayerMessage {
    static instance = new WaitingForAGame();

    constructor() {
        super();
    }

    public static fromJson(): WaitingForAGame {
        return WaitingForAGame.instance;
    }
}

export class ItIsNotYourStepError extends ToPlayerMessage {
    static instance = new ItIsNotYourStepError();

    constructor() {
        super();
    }

    public static fromJson(): ItIsNotYourStepError {
        return ItIsNotYourStepError.instance;
    }
}

export class PlayerInfo {
    public readonly color: FigureColor;

    constructor(
        public readonly index: number,
        public readonly name: string
    ) {
        this.color = colorOfPlayerByIndex(index);
    }

    public static fromJson(json: any): PlayerInfo {
        let index_: number = getFromJson<number>(json, 'index');
        let name_ : string = getFromJson<string>(json, 'name' );
        return new PlayerInfo(index_, name_);
    }
}

export class NextMoveInfo extends ToPlayerMessage {
    constructor(
        public readonly newBoard: Board,
        public readonly lastMove: PlayerMove
    ) { super(); }

    public static fromJson(json: any): NextMoveInfo {
        let newBoard_:      Board =      Board.fromJson(getFromJson<any>(json, 'newBoard'));
        let lastMove_: PlayerMove = PlayerMove.fromJson(getFromJson<any>(json, 'lastMove'));
        return new NextMoveInfo(newBoard_, lastMove_);
    }
}

export class WrongMoveError extends ToPlayerMessage {
    constructor(public readonly move: PlayerMove) {
        super();
    }

    public static fromJson(json: any): WrongMoveError {
        let move_: PlayerMove = PlayerMove.fromJson(getFromJson<any>(json, 'move'));
        return new WrongMoveError(move_);
    }
}

export class GameInfo extends ToPlayerMessage {
    constructor(
        public readonly gameId: number,
        public readonly players: PlayerInfo[],
        public readonly you: number,
        public readonly board: Board,
        public readonly history: GameHistory
    ) {
        super();
    }

    public static fromJson(json: any): GameInfo {
        let gameId_ : number       =                getFromJson<number>(json, 'gameId' );
        let players_: PlayerInfo[] =                getFromJson<any[]> (json, 'players').map(jsonPl => PlayerInfo.fromJson(jsonPl));
        let you_    : number       =                getFromJson<number>(json, 'you'    );
        let board_  : Board        = Board.fromJson(getFromJson<any>   (json, 'board'  ));
        let history_: GameHistory  =                getFromJson<any[]> (json, 'history').map(jsonHist => BoardHistoryItem.fromJson(jsonHist));

        return new GameInfo(gameId_, players_, you_, board_, history_);
    }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
class WebServiceIncomeMessage {}

export class PlayerMoveInfo extends WebServiceIncomeMessage {
    constructor(
        public readonly turn: number,
        public readonly cellsQueue: P[]
    ) {
        super();
    }
}

export class ResetGameMessage extends WebServiceIncomeMessage {
    constructor(public readonly turn: number) {
        super();
    }
}

export class ResignGameMessage extends WebServiceIncomeMessage {
    constructor(public readonly turn: number) {
        super();
    }
}

/////////////////////////////////////////////////////
export const toPlayerMessageFactoryMap  = {
    "WaitingForAGame" : WaitingForAGame,
    "ItIsNotYourStepError": ItIsNotYourStepError,
    "NextMoveInfo": NextMoveInfo,
    "WrongMoveError": WrongMoveError,
    "GameInfo": GameInfo
}

export class WebServiceOutcomeMessage {
    constructor(
        public readonly gameId: number,
        public readonly msgType: String,
        public readonly msg: ToPlayerMessage
    ) {}

    public static fromJson(json: any): WebServiceOutcomeMessage {
        try {
            const toPlayerMessage: ToPlayerMessage = WebServiceOutcomeMessage.parseToPlayerMessageByType(json.msgType, json.msg);
            return new WebServiceOutcomeMessage(json.gameId, json.msgType, toPlayerMessage);
        } catch (err) {
            throw new GameMessageParsingError(json, err);
        }
    }

    private static parseToPlayerMessageByType(msgType: string, json: any): ToPlayerMessage {
        const factory = toPlayerMessageFactoryMap[msgType]
        if (factory === undefined) {
            throw new ClassCastException(`The type "${msgType}" is unrecognized! Supported types are: [GameInfo, NextMoveInfo, WaitingForAGame, WrongMoveError, ItIsNotYourStepError].`, msgType);
        }
        return factory.fromJson(json);
    }
}
