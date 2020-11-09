package tga.checkers.web.websocket.dto

import java.time.Instant

/**
 * DTO for storing a user's activity.
 */
class ActivityDTO {
    var sessionId: String? = null
    var userLogin: String? = null
    var ipAddress: String? = null
    var page: String? = null
    var time: Instant? = null

    // prettier-ignore
    override fun toString(): String {
        return "ActivityDTO{" +
            "sessionId='" + sessionId + '\'' +
            ", userLogin='" + userLogin + '\'' +
            ", ipAddress='" + ipAddress + '\'' +
            ", page='" + page + '\'' +
            ", time='" + time + '\'' +
            '}'
    }
}
