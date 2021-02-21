package com.salesianostriana.dam.tvseries.security.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Componente que indica cómo comenzar el esquema de autenticación y define el reino de seguridad
 */
@Component
class JwtAuthenticationEntryPoint(
        val mapper : ObjectMapper
)  : AuthenticationEntryPoint {


    override fun commence(request: HttpServletRequest?, response: HttpServletResponse?, authException: AuthenticationException?) {

        response?.status = 401
        response?.contentType = "application/json"

        /**
         * Si usáramos una clase para modelar la respuesta a las peticiones de error (tipo ApiError) podríamos usarla
         * aquí para dar respuesta. Para ello, podríamos usar el mapper que se pasa aquí como argumento
         */
        // Respuesta como texto plano, no JSON
        // response?.writer?.println(authException?.message)

        // Respuesta como un JSON sencillo
        response?.writer?.println(mapper.writeValueAsString(authException?.message?.let { MensajeError(it) }))

    }

}

data class MensajeError(
    val mensaje: String
)