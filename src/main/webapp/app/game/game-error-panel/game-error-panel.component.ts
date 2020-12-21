import {Component, Input, OnInit} from '@angular/core';
import {ToPlayerMessage} from "app/core/game-maker/ExternalMessages";

@Component({
    selector: 'jhi-game-error-panel',
    templateUrl: './game-error-panel.component.html',
    styleUrls: ['./game-error-panel.component.scss']
})
export class GameErrorPanelComponent implements OnInit {

    @Input() error!: ToPlayerMessage

    constructor() {
    }

    ngOnInit(): void {

    }

    public errClass(err: ToPlayerMessage): string {
        return err.constructor.name
    }

}
