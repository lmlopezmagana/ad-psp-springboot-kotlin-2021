package com.salesianostriana.dam.ejerciciojpakotlin

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.validator.constraints.URL
import java.io.Serializable
import java.util.*
import javax.persistence.*
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@Entity
class Categoria(
    @get:NotBlank(message="{categoria.nombre.blank}") var nombre: String,
    @get:URL var urlImagen: String,
    @ManyToOne var padre: Categoria? = null,
    @Id @GeneratedValue val id : Long? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Categoria
        if (id != that.id) return false
        return true
    }


    override fun hashCode(): Int {
        return if (id != null)
            id.hashCode()
        else 0
    }


}

@Entity
class Producto(
    @get:NotBlank(message="{producto.nombre.blank}")
    var nombre: String,

    @get:NotNull(message="{producto.precio.null}")
    @get:Min(value=0)
    var precio: Double,

    @Lob
    var descripcion: String,

    @get:URL
    var urlImage: String,

    @ManyToOne
    @get:NotNull(message="{producto.categoria.null}")
    var categoria: Categoria,

    @Id @GeneratedValue val id : Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Producto
        if (id != that.id) return false
        return true
    }


    override fun hashCode(): Int {
        return if (id != null)
            id.hashCode()
        else 0
    }
}


@Entity
class Usuario(
        var username : String,
        var password: String,
        var email: String,
        var fullName: String,
        @Id @GeneratedValue val id : Long? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Usuario
        if (id != that.id) return false
        return true
    }


    override fun hashCode(): Int {
        return if (id != null)
            id.hashCode()
        else 0
    }
}

@Embeddable
class ValoracionPK(
        @Column(name="producto_id") var productoId: Long,
        @Column(name="usuario_id") var usuarioId: Long
) :Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as ValoracionPK
        return Objects.equals(productoId, that.productoId)
                && Objects.equals(usuarioId, that.usuarioId)
    }

    override fun hashCode(): Int {
        return if (productoId != null && usuarioId != null)
            Objects.hash(productoId, usuarioId)
        else 0
    }

}

@Entity
class Valoracion(
        @EmbeddedId var id: ValoracionPK,

        @ManyToOne
        @MapsId("productoId")
        @JoinColumn(name="producto_id")
        var producto: Producto,

        @ManyToOne
        @MapsId("usuarioId")
        @JoinColumn(name="usuario_id")
        var usuario: Usuario,

        var meGusta: Boolean,

        var valoracion: Int

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as Valoracion
        if (id != that.id) return false
        return true
    }


    override fun hashCode(): Int {
        return if (id != null)
            id.hashCode()
        else 0
    }
}