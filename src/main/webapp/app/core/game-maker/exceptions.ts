export type ThrowableCause = Throwable | any | null | undefined;

export class Throwable {
    constructor(public readonly message: string, public readonly cause?: ThrowableCause) {}

    toString(): string {
        const thisClass = typeof this;
        let thisStr = `${thisClass}: ${this.message}`;
        if (this.cause) {
            if (this.cause instanceof Throwable) {
                thisStr += '\n\t caused by: ' + this.cause?.toString();
            } else {
                thisStr += '\n\t caused by: ' + this.cause;
            }
        }
        return thisStr;
    }
}

export class RuntimeException extends Throwable {
    constructor(message: string, cause?: ThrowableCause) {
        super(message, cause);
    }
}

export class ClassCastException extends Throwable {
    constructor(message: string, cause?: ThrowableCause) {
        super(message, cause);
    }
}
