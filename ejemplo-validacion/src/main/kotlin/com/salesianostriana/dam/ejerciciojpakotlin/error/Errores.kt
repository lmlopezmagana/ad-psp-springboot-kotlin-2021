package com.salesianostriana.dam.ejerciciojpakotlin

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.validator.internal.engine.path.PathImpl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.Exception
import java.lang.RuntimeException
import java.time.LocalDateTime
import javax.validation.ConstraintViolationException

/**
 * Clase que permite devolver el error en un foramto JSON más adecuado
 *  - Estado
 *  - Mensaje
 *  - Fecha y hora
 *  - Lista de suberrores (si es que los hay, como en la validación)
 */
data class ApiError(
    val estado: HttpStatus,
    val mensaje: String?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val subErrores: List<out ApiSubError>? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy hh:mm:ss")
    val fecha: LocalDateTime = LocalDateTime.now(),
)

/**
 * Clase base para los suberrores. Ahora mismo extendida por los
 * suberrores de validación, pero con posibilidad de ser extendida
 * por otras clases.
 */
open abstract class ApiSubError


/**
 * Mensaje concreto en un error de validación
 * Por ejemplo, un campo que está vacío y no debe estarlo,
 * un valor numérico que no cumple una condición, ...
 *  - Objeto: clase, entidad o modelo sobre el que sucede
 *  - Campo: atributo del objeto sobre el que se produce el error
 *  - Valor rechazado: valor inválido
 *  - Mensaje: mensaje de error (si está configurado, viene del fichero messages.properties)
 */
data class ApiValidationSubError(
    val objeto : String,
    val campo : String,
    val valorRechazado : Any?,
    val mensaje : String?
) : ApiSubError()



