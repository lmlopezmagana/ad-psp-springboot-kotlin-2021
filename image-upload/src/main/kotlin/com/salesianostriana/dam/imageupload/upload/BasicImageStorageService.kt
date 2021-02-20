package com.salesianostriana.dam.imageupload.upload

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path
import java.util.*
import java.util.stream.Stream

interface BasicImageStorageService<T, ID, DID> {

    fun store(file : MultipartFile) : Optional<T>

    fun loadAsResource(id : ID) : Optional<MediaTypeUrlResource>

    fun delete(id : DID)


}

