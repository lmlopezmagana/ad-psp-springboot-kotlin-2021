package com.salesianostriana.dam.imageupload.upload

import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.*
import javax.print.attribute.standard.Media

@RestController
class ImgurFilesController(
        private val imgurStorageService: ImgurStorageService
) {

    @Throws(ResponseStatusException::class)
    @GetMapping("/files/{id}")
    fun get(@PathVariable id: String) : ResponseEntity<Resource> {
        var resource: Optional<MediaTypeUrlResource>
        try {
            resource = imgurStorageService.loadAsResource(id)
            if (resource.isPresent) {
                return ResponseEntity.ok().contentType(MediaType.parseMediaType(resource.get().mediaType)).body(resource.get())
            }
            return ResponseEntity.noContent().build()
        } catch (ex: ImgurImageNotFoundException) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Imagen no encontrada")
        }

    }


}

