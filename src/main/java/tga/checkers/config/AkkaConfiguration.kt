package tga.checkers.config

import akka.actor.ActorSystem
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AkkaConfiguration {

    @Bean
    fun actorSystem(): ActorSystem {
        return ActorSystem.create("AkkaSystem")
    }


}
