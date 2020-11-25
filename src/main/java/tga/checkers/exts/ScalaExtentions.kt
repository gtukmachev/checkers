package tga.checkers.exts

import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.ActorRefFactory
import akka.actor.Props
import akka.japi.Creator
import akka.japi.pf.FI
import akka.japi.pf.ReceiveBuilder
import kotlin.reflect.KClass

fun <T : Actor> ActorRefFactory.actorOf( actorName: String, actorCreator: () -> T): ActorRef {

    val creator = LambdaCreator(actorCreator)

    return this.actorOf(Props.create( creator ), actorName)
}

fun Actor.tellToSelfAfter(delay: java.time.Duration, msgProducer: () -> Any) {
    val msg = msgProducer()
    context().system().scheduler().scheduleOnce(delay, self(), msg, context().dispatcher(), self())
}

fun Actor.tellAfter(delay: java.time.Duration, actorRef : ActorRef, msgProducer: () -> Any) {
    val msg = msgProducer()
    context().system().scheduler().scheduleOnce(delay, actorRef, msg, context().dispatcher(), self())
}


class LambdaCreator<T>(private val actorCreator: () -> T) : Creator<T> {
    override fun create(): T {
        return actorCreator.invoke()
    }
}

class PredicateCreator<T>(val predicateFunction: (T) -> Boolean ) : FI.TypedPredicate<T> {
    override fun defined(t: T): Boolean {
        return predicateFunction(t)
    }
}

fun <P : Any> ReceiveBuilder.on(clazz: KClass<P>, apply: (P) -> Unit): ReceiveBuilder {
    return this.match(clazz.java, apply)
}

fun <P : Any> ReceiveBuilder.on(clazz: KClass<P>, predicate: (P) -> Boolean, apply: (P) -> Unit): ReceiveBuilder {
    return this.match(clazz.java, PredicateCreator(predicate), apply)
}
