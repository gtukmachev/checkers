package tga.checkers.web.websocket

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageSendingOperations
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import tga.checkers.config.WebsocketConfiguration
import tga.checkers.web.websocket.dto.ActivityDTO
import java.security.Principal
import java.time.Instant

@Controller
class ActivityService(private val messagingTemplate: SimpMessageSendingOperations) : ApplicationListener<SessionDisconnectEvent> {

    @MessageMapping("/topic/activity")
    @SendTo("/topic/tracker")
    fun sendActivity(@Payload activityDTO: ActivityDTO, stompHeaderAccessor: StompHeaderAccessor, principal: Principal): ActivityDTO {
        activityDTO.userLogin = principal.name
        activityDTO.sessionId = stompHeaderAccessor.sessionId
        activityDTO.ipAddress = stompHeaderAccessor.sessionAttributes[WebsocketConfiguration.IP_ADDRESS].toString()
        activityDTO.time = Instant.now()
        log.debug("Sending user tracking data {}", activityDTO)
        return activityDTO
    }

    override fun onApplicationEvent(event: SessionDisconnectEvent) {
        val activityDTO = ActivityDTO()
        activityDTO.sessionId = event.sessionId
        activityDTO.page = "logout"
        messagingTemplate.convertAndSend("/topic/tracker", activityDTO)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ActivityService::class.java)
    }
}
