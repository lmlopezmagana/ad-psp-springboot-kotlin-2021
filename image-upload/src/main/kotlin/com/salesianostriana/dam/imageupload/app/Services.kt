package com.salesianostriana.dam.imageupload.app

import com.salesianostriana.dam.imageupload.upload.BasicImageStorageService
import com.salesianostriana.dam.imageupload.upload.ImgurImageAttribute
import com.salesianostriana.dam.imageupload.upload.ImgurStorageService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

open class BaseService<T, ID, R : JpaRepository<T, ID>>  {

    @Autowired
    protected lateinit var repository: R

    open fun save(t : T) = repository.save(t)
    open fun findAll() : List<T> = repository.findAll()
    open fun findById(id : ID) = repository.findById(id)
    open fun edit(t : T) = save(t)

    open fun deleteById(id : ID) {
        findById(id).ifPresent { this.delete(it) }
    }

    open fun delete(t : T) = repository.delete(t)

    open fun deleteAll() = repository.deleteAll()

}

@Service
class EntidadServicio(
        private val imageStorageService: ImgurStorageService
) : BaseService<Entidad, Long, EntidadRepository>() {

    val logger: Logger = LoggerFactory.getLogger(EntidadServicio::class.java)


    fun save(e: Entidad, file: MultipartFile) : Entidad {
        var imageAttribute : Optional<ImgurImageAttribute> = Optional.empty()
        if (!file.isEmpty) {
            imageAttribute = imageStorageService.store(file)
        }

        e.img = imageAttribute.orElse(null)
        return save(e)
    }

    override fun delete(e : Entidad) {
        logger.debug("Eliminando la entidad $e")
        e.img?.let { it.deletehash?.let { it1 -> imageStorageService.delete(it1) } }
        super.delete(e)
    }

}