package com.salesianostriana.dam.ejerciciojpakotlin.error

import com.salesianostriana.dam.ejerciciojpakotlin.ApiError
import com.salesianostriana.dam.ejerciciojpakotlin.ApiValidationSubError
import org.hibernate.validator.internal.engine.path.PathImpl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.Exception
import javax.validation.ConstraintViolationException


/**
 *
 * La anotación @ControllerAdvice aparece en la versión 3.2 de Spring y consiste en una especialización de la anotación
 * @Component que permite declarar métodos relacionados con el manejo de excepciones que serán compartidos entre
 * múltiples controladores, evitando así la duplicidad de código o la generación de jerarquías para que los
 * controladores traten de manera homogénea las excepciones.
 *
 * Por otro lado, la anotación @RestControllerAdvice aparece por primera vez en la versión 4.3 de Spring y se
 * trata de una anotación que aúna @ControllerAdvice y @ResponseBody. Su funcionamiento es prácticamente idéntico a
 * @ControllerAdvice, aunque su uso está enfocado a APIs REST, con el agregado de permitirnos establecer
 * un contenido para el cuerpo de las respuestas los casos de error contemplados.
 *
 * La anotación @ExceptionHandler va a permitir asociar los métodos de esta clase con determinadas excepciones,
 * de forma que cuando se lance una excepción de ese tipo en alguno de los controladores, será tratada por este
 * bloque de código.
 *
 * Sobrescribir el método handleExceptionInternal(...) es una manera adecuada de unificar el mensaje de
 * respuesta a errores que vamos a dar, a través de una serie de clases de error.
 */
@RestControllerAdvice
class GlobalRestControllerAdvice : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value=[EntityNotFoundException::class])
    fun handleNotFoundException(ex: EntityNotFoundException) =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiError(HttpStatus.NOT_FOUND, ex.message))


    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ) : ResponseEntity<Any> =
        ResponseEntity
            .status(status)
            .body(
                ApiError(
                    status,
                    "Error de validación (handleMethodArgumentNotValid)",
                    ex.fieldErrors.map {
                        ApiValidationSubError(it.objectName, it.field, it.rejectedValue, it.defaultMessage)
                    }
                )
            )


    @ExceptionHandler(value=[ConstraintViolationException::class])
    fun handleConstraintViolation(ex : ConstraintViolationException) : ResponseEntity<ApiError> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiError(
                    HttpStatus.BAD_REQUEST,
            "Error de validación (handleConstraintViolation)",
                    ex.constraintViolations
                    .map { ApiValidationSubError(
                    it.rootBeanClass.simpleName, (it.propertyPath as PathImpl).leafNode.asString(), it.invalidValue, it.message)
                    }
                )
            )



    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val apiError = ApiError(status, ex.message)
        return ResponseEntity.status(status).body(apiError)
    }


}