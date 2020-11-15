package tga.checkers.config

import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component

@Component
class WebSocketInboundChannelInterceptor : ChannelInterceptor {

    companion object {
        private val log = LoggerFactory.getLogger(WebSocketInboundChannelInterceptor::class.java)
    }

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        log.trace("preSend( message = '$message', channel = '$channel')")

        val accessor: StompHeaderAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)!!
/*
        when(accessor.command) {
            StompCommand.CONNECT -> {
                val username = accessor.getFirstNativeHeader("login")
                val password = accessor.getFirstNativeHeader("pass")
                val chat = accessor.getFirstNativeHeader("chat")
                val user = authService.getAuthenticatedOrFail(username, password, chat)
                accessor.user = user
            }
            StompCommand.SUBSCRIBE -> {
                authService.allowSubscriptionOrRiseError(accessor.user, accessor.destination)
                userService.addUser(accessor.user, accessor.destination)
            }
            StompCommand.SEND      -> authService.allowSendOrRiseError(accessor.user, accessor.destination)
            else -> { }
        }
*/

        return message
    }
}
