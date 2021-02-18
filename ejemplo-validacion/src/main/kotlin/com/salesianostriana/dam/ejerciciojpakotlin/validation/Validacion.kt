package com.salesianostriana.dam.ejerciciojpakotlin

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean


/**
 * Clase que permite configurar la validación basada en un fichero de properties.
 * De esta forma, los mensajes de error se pueden definir en un fichero de properties.
 * Este permite además poder internacionalizar los mensajes de error.
 */
@Configuration
class ConfiguracionValidacion {

    @Bean
    fun messageSource() : MessageSource {
        var messageSource = ReloadableResourceBundleMessageSource()

        messageSource.setBasename("classpath:messages")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }


    @Bean
    fun getValidator() : LocalValidatorFactoryBean {
        val validator = LocalValidatorFactoryBean()
        validator.setValidationMessageSource(messageSource())
        return validator
    }



}