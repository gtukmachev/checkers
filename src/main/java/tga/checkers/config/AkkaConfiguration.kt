package tga.checkers.config

import akka.actor.ActorRef
import akka.actor.ActorSystem
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.SimpMessagingTemplate
import tga.checkers.game.actors.GamesMakerActor
import tga.checkers.exts.actorOf

@Configuration
class AkkaConfiguration {

    @Bean fun akka(): ActorSystem {
        return ActorSystem.create("AkkaSystem")
    }

    @Bean fun gameMakerActor(akka: ActorSystem, messagingTemplate: SimpMessagingTemplate): ActorRef {
        return akka.actorOf("gameMaker"){ GamesMakerActor(messagingTemplate) }
    }

}
