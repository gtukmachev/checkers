package tga.checkers.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer
import tga.checkers.security.AuthoritiesConstants

@Configuration
class WebsocketSecurityConfiguration : AbstractSecurityWebSocketMessageBrokerConfigurer() {

    override fun configureInbound(messages: MessageSecurityMetadataSourceRegistry) {
        messages
            .nullDestMatcher().authenticated()
            .simpDestMatchers("/topic/tracker").hasAuthority(AuthoritiesConstants.ADMIN) // matches any destination that starts with /topic/
            .simpDestMatchers("/topic/tracker").hasAuthority(AuthoritiesConstants.ADMIN) // matches any destination that starts with /topic/
            .simpSubscribeDestMatchers("/user/queue/new-game-request").hasAuthority(AuthoritiesConstants.USER)

            // (i.e. cannot send messages directly to /topic/)
            // (i.e. cannot subscribe to /topic/messages/* to get messages sent to
            // /topic/messages-user<id>)
            .simpDestMatchers("/topic/**").authenticated() // message types other than MESSAGE and SUBSCRIBE
            .simpTypeMatchers(SimpMessageType.MESSAGE, SimpMessageType.SUBSCRIBE).denyAll() // catch all
            .anyMessage().denyAll()
    }

    /**
     * Disables CSRF for Websockets.
     */
    override fun sameOriginDisabled(): Boolean {
        return true
    }
}


