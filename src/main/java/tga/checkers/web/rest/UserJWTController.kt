package tga.checkers.web.rest

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tga.checkers.security.jwt.JWTFilter
import tga.checkers.security.jwt.TokenProvider
import tga.checkers.web.rest.vm.LoginVM
import javax.validation.Valid

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
class UserJWTController(private val tokenProvider: TokenProvider, private val authenticationManagerBuilder: AuthenticationManagerBuilder) {

    @PostMapping("/authenticate")
    fun authorize(@RequestBody loginVM: @Valid LoginVM): ResponseEntity<JWTToken> {
        val authenticationToken = UsernamePasswordAuthenticationToken(loginVM.username, loginVM.password)
        val authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken)

        SecurityContextHolder.getContext().authentication = authentication

        val rememberMe = if (loginVM.isRememberMe == null) false else loginVM.isRememberMe

        val jwt = tokenProvider.createToken(authentication, rememberMe)

        val httpHeaders = HttpHeaders()

        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer $jwt")

        return ResponseEntity( JWTToken(jwt), httpHeaders, HttpStatus.OK )
    }

    /**
     * Object to return as body in JWT Authentication.
     */
}

data class JWTToken( @get:JsonProperty("id_token") var idToken: String )
