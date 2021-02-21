package com.salesianostriana.dam.tvseries.security.jwt

import com.salesianostriana.dam.tvseries.users.User
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*


@Component
class JwtTokenProvider {

    // Algunas constantes de utilidad
    companion object {
        const val TOKEN_HEADER = "Authorization"
        const val TOKEN_PREFIX = "Bearer "
        const val TOKEN_TYPE = "JWT"
    }

    // Algunos de estos valores deberían cargarse desde el fichero de properties, y estos a su vez desde variables
    // de entorno
    private val jwtSecreto : String = "mJI.w|g!kCv(5bLr0A@\"wTC,N9mNM]Dd^19h0[?!KB1~I~kfA(,;T<S][_Pm_v(asdfghasdfg"
    // Lo expresamos en días
    private val jwtDuracionToken : Long = 3
    private val jwtDuracionRefreshToken : Long = 10

    // Clase que se encarga del parseo de tokens
    private val parser = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecreto.toByteArray())).build()

    private val logger : Logger = LoggerFactory.getLogger(JwtTokenProvider::class.java)

    /**
     * Método que se encarga de generar el token de autenticación
     * para el usuario actualmente autenticado
     */
    fun generateToken(authentication : Authentication) : String {
        val user : User = authentication.principal as User
        return generateTokens(user, false)
    }

    /**
     * Método encargado de generar el token de autenticación para un
     * determinado usuario
     */
    fun generateToken(user : User) = generateTokens(user, false)


    /**
     * Método encargado de generar el token de refresco para el
     * usuario actualmente autenticado
     */
    fun generateRefreshToken(authentication: Authentication) : String {
        val user : User = authentication.principal as User
        return generateTokens(user, true)
    }

    /**
     * Método encargado de generar el token de refresco para un usuario
     */
    fun generateRefreshToken(user : User) = generateTokens(user, true)

    /**
     * Método encargado de generar el token para un usuario.
     */
    private fun generateTokens(user : User, isRefreshToken : Boolean) : String {
        val tokenExpirationDate =
                Date.from(Instant.now().plus(if (isRefreshToken) jwtDuracionRefreshToken else jwtDuracionToken, ChronoUnit.DAYS))
        val builder = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(jwtSecreto.toByteArray()), SignatureAlgorithm.HS512)
                .setHeaderParam("typ", TOKEN_TYPE)
                .setSubject(user.id.toString())
                .setExpiration(tokenExpirationDate)
                .setIssuedAt(Date())
                .claim("refresh", isRefreshToken)

        if (!isRefreshToken) {
            builder
                    .claim("fullname", user.fullName)
                    .claim("roles", user.roles.joinToString())

        }
        return builder.compact()
    }

    /**
     * Método que recibe un token y devuelve el identificador del usuario
     */
    fun getUserIdFromJWT(token: String): UUID = UUID.fromString(parser.parseClaimsJws(token).body.subject)

    /**
     * Método que recibe un token de refresco y lo valida
     */
    fun validateRefreshToken(token : String) = validateToken(token, true)

    /**
     * Método que recibe un token de autenticación y lo valida
     */
    fun validateAuthToken(token : String) = validateToken(token, false)


    /**
     * Método privado usado para validar un token. Comprueba que no hay error en la firma,
     * que está bien formado, que no ha expirado, que está completo y soportado.
     * Además, comprueba que es el tipo de token adecuado, es decir, que no estamos
     * usando el token de refresco para hacer peticiones.
     */
    private fun validateToken(token : String, isRefreshToken: Boolean) : Boolean {
        try {
            val claims = parser.parseClaimsJws(token)
            if (isRefreshToken != claims.body["refresh"])
                throw UnsupportedJwtException("No se ha utilizado el token apropiado")
            return true
        } catch (ex : Exception) {
            with(logger) {
                when (ex) {
                    is SignatureException -> info("Error en la firma del token ${ex.message}")
                    is MalformedJwtException -> info("Token malformado ${ex.message}")
                    is ExpiredJwtException -> info("Token expirado ${ex.message}")
                    is UnsupportedJwtException -> info("Token no soportado ${ex.message}")
                    is IllegalArgumentException -> info("Token incompleto (claims vacío) ${ex.message}")
                    else -> info("Error indeterminado")
                }
            }

        }

        return false

    }

}