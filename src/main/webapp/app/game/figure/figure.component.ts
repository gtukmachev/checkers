import { Component, Input, OnInit } from '@angular/core';
import { Figure, FigureColor, FigureType, o } from 'app/core/game-maker/GameMessages';
import { faCircle } from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'jhi-figure',
    templateUrl: './figure.component.html',
    styleUrls: ['./figure.component.scss'],
})
export class FigureComponent implements OnInit {
    @Input() figure: Figure | null = o;

    public faCircle = faCircle;
    public FigureColor_WHITE = FigureColor.WHITE;
    public FigureColor_BLACK = FigureColor.BLACK;
    public FigureType_QUINN = FigureType.QUINN;

    constructor() {}

    ngOnInit(): void {
        console.log('figure = ', this.figure);
        console.log('FigureColor_WHITE = ', this.FigureColor_WHITE);
        console.log('FigureColor_BLACK = ', this.FigureColor_BLACK);
    }
}
