package com.salesianostriana.dam.ejerciciojpakotlin.data

import com.fasterxml.jackson.annotation.JsonInclude
import com.salesianostriana.dam.ejerciciojpakotlin.Categoria
import org.hibernate.validator.constraints.URL
import javax.validation.constraints.NotBlank

data class EditCategoriaDto(
    @get:NotBlank(message="{categoria.nombre.blank}") var nombre: String,
    @get:URL var urlImagen: String,
    var padre: Long? = null
)

data class GetCategoriaDtoPadre(
    var id: Long?,
    var nombre: String?
)

data class GetCategoriaDto(
    var id: Long?,
    var nombre: String,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var urlImagen: String,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var padre: GetCategoriaDtoPadre?
)

data class GetCategoriaDtoSinPadre(
    var id: Long?,
    var nombre: String,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var urlImagen: String,
)

data class GetCategoriaDetalleDto(
    var id: Long?,
    var nombre: String,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var urlImagen: String,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var padre: GetCategoriaDtoSinPadre?
)

fun Categoria.toGetCategoriaDto() : GetCategoriaDto {
    var padreDto : GetCategoriaDtoPadre? = null
    if (padre != null)
        padreDto = GetCategoriaDtoPadre(padre!!.id, padre!!.nombre)
    return GetCategoriaDto(id, nombre, urlImagen, padreDto)
}

fun Categoria.toGetCategoriaDetalleDto() : GetCategoriaDetalleDto {
    var padreDto: GetCategoriaDtoSinPadre? = null
    if (padre != null)
        padreDto = GetCategoriaDtoSinPadre(padre!!.id, padre!!.nombre, padre!!.urlImagen)
    return GetCategoriaDetalleDto(id, nombre, urlImagen, padreDto)
}
