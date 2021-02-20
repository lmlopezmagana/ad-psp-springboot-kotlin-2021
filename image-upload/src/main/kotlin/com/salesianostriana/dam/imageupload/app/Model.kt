package com.salesianostriana.dam.imageupload.app

import com.salesianostriana.dam.imageupload.upload.ImgurImageAttribute
import org.springframework.data.jpa.repository.JpaRepository
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@Entity
data class Entidad(
        var texto : String,
        var img : ImgurImageAttribute? = null,
        @Id @GeneratedValue val id: Long? = null
)

interface EntidadRepository : JpaRepository<Entidad, Long> {

}




