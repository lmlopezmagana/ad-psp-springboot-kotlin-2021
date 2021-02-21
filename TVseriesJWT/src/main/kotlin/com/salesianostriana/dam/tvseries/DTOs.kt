package com.salesianostriana.dam.tvseries

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.util.*

data class SerieDTO(
        val id: UUID?,
        val nombre: String,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        val fechaEstreno: LocalDate,
        val plataforma: Plataforma,
        val numeroTemporadas : Int
        )

fun Serie.toSerieDTO() = SerieDTO(id, nombre, fechaEstreno, plataforma, numeroTemporadas)

fun SerieDTO.toSerie() = Serie(nombre, fechaEstreno,plataforma, numeroTemporadas, id)

data class NuevaSerieDTO(
        val nombre: String,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        val fechaEstreno: LocalDate,
        val plataforma: Plataforma,
        val numeroTemporadas : Int
)

fun NuevaSerieDTO.toSerie() = Serie(nombre, fechaEstreno, plataforma, numeroTemporadas)


data class CapituloDTO(
        val id : UUID?,
        val nombre: String,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        val fechaEstreno: LocalDate
)

fun Capitulo.toCapituloDTO() = CapituloDTO(id, nombre, fechaEstreno)

fun CapituloDTO.toCapitulo() = Capitulo(nombre, fechaEstreno, null, id)

