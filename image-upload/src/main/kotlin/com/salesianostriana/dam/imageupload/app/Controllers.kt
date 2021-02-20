package com.salesianostriana.dam.imageupload.app

import com.salesianostriana.dam.imageupload.upload.ImgurBadRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/entity/")
class UploadController(
        private val servicio: EntidadServicio
) {

    @GetMapping("/")
    fun getAll() : List<EntidadDto> {
        val result = servicio.findAll()
        if (result.isEmpty())
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No hay registros")
        return result.map { it.toDto() }

    }

    @GetMapping("/{id}")
    fun getOne(@PathVariable id: Long) : EntidadDto =
            servicio.findById(id).map { it.toDto() }
                    .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Entidad $id no encontrada") }


    @PostMapping("/")
    fun create(@RequestPart("nuevo") new : NuevaEntidadDto, @RequestPart("file") file: MultipartFile ) : ResponseEntity<EntidadDto> {

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(servicio.save(new.toEntidad(), file).toDto())
        } catch ( ex : ImgurBadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la subida de la imagen")
        }

    }


    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) : ResponseEntity<Void> {
        servicio.deleteById(id)
        return ResponseEntity.noContent().build()
    }


}


data class NuevaEntidadDto(
        var texto: String
)

fun NuevaEntidadDto.toEntidad() = Entidad(texto)

data class EntidadDto(
        val texto: String,
        val imageId: String?,
        val id: Long?
)

fun Entidad.toDto() = EntidadDto( texto, img?.id, id)