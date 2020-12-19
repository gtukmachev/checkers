import { Throwable, ThrowableCause } from './exceptions';

export class AttributeMissedException extends Throwable {
    constructor(public readonly json: any, public readonly attr: string, cause?: ThrowableCause) {
        super(`The json doesn't contain the '${attr}' attribute. json=${JSON.stringify(json)}`, cause);
    }
}

export function getFromJson<T>(json: any, attr: string): T {
    let value = json[attr];
    if (value) {
        return value as T;
    }
    throw new AttributeMissedException(json, attr);
}
