package com.salesianostriana.dam.tvseries

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/series")
class SerieController(val serieRepository : SerieRepository, val capituloRepository: CapituloRepository) {


    private fun todasLasSeries(conCapitulos : Boolean) : List<Serie> {
        var result: List<Serie>
        with(serieRepository) {
            result = when (conCapitulos) {
                true -> findAllConCapitulos()
                false -> findAll()
            }
        }
        if (result.isEmpty())
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No hay series almacenadas")
        return result
    }

    private fun unaSerie(id: UUID, conCapitulos: Boolean) : Serie {
        var result: Optional<Serie>
        with(serieRepository) {
            result = when(conCapitulos) {
                true -> findByIdConCapitulos(id)
                false -> findById(id)
            }
        }
        return result.orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado la serie con el identificador $id")}
    }

    @GetMapping("/")
    fun todas()  = todasLasSeries(false).map { it.toSerieDTO() }

    @GetMapping("/capitulos")
    fun todasConCapitulos() = todasLasSeries(true)

    @GetMapping("/{id}")
    fun unaSerie(@PathVariable id : UUID) = unaSerie(id, false).toSerieDTO()

    @GetMapping("/{id}/capitulos")
    fun unaSerieConCapitulos(@PathVariable id: UUID) = unaSerie(id, true)

    @PostMapping("/")
    fun nuevaSerie(@RequestBody nuevaSerie: NuevaSerieDTO) =
            ResponseEntity.status(HttpStatus.CREATED).body(serieRepository.save(nuevaSerie.toSerie()).toSerieDTO())

    @PutMapping("/{id}")
    fun editarSerie(@RequestBody editarSerie: NuevaSerieDTO, @PathVariable id : UUID): SerieDTO {
        return serieRepository.findById(id)
                .map { serieEncontrada  ->
                        val serieActualizada : Serie =
                                serieEncontrada.copy(nombre = editarSerie.nombre,
                                    fechaEstreno = editarSerie.fechaEstreno,
                                    numeroTemporadas = editarSerie.numeroTemporadas,
                                    plataforma = editarSerie.plataforma)
                        serieRepository.save(serieActualizada).toSerieDTO()
                }.orElseThrow {
                    ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado la serie con el identificador $id")
                }
    }

    @DeleteMapping("/{id}")
    fun eliminarSerie(@PathVariable id : UUID) : ResponseEntity<Void> {
        serieRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/capitulos")
    fun nuevoCapitulo(@PathVariable id: UUID, @RequestBody nuevoCapitulo: CapituloDTO) {
        serieRepository.findById(id)
                .map { serie -> {
                    val capitulo = nuevoCapitulo.toCapitulo()
                    capitulo.serie = serie
                    ResponseEntity.status(HttpStatus.CREATED).body(capituloRepository.save(capitulo).toCapituloDTO())
                } }.orElseThrow {
                    ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado la serie con el identificador $id")
                }
    }

}