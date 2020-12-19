import { P } from '../../../../../main/webapp/app/core/game-maker/GameStateData';

fdescribe('fromJson() tests', () => {
    let jsonStr = '{"l":"1", "c":"1"}';
    let jsonObj = JSON.parse(jsonStr);

    let p = P.fromJson(jsonObj);

    expect(p).toEqual(new P(1, 1));
    /*
    describe("P() class", () => {
        it("should be parsed correctly from correct json:", () => {
        })
    })
*/
});
