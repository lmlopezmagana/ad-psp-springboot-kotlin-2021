package com.salesianostriana.dam.tvseries

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import java.time.LocalDate
import java.util.*
import javax.persistence.*

enum class Plataforma {
    NETFLIX, HBO, MOVISTAR, DISNEY, AMAZON
}

@Entity
data class Serie(
        var nombre: String,
        var fechaEstreno: LocalDate,

        @Enumerated(EnumType.STRING) var plataforma: Plataforma,
        var numeroTemporadas: Int,


        @Id @GeneratedValue val id: UUID? = null,

        @JsonManagedReference
        @OneToMany(mappedBy="serie", fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
        var capitulos : List<Capitulo>? = null

)

@Entity
data class Capitulo(
        var nombre: String,
        var fechaEstreno: LocalDate,
        @JsonBackReference @ManyToOne var serie: Serie? = null,
        @Id @GeneratedValue val id: UUID? = null
)