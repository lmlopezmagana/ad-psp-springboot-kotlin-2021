package com.salesianostriana.dam.tvseries

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*
import javax.annotation.PostConstruct

interface SerieRepository : JpaRepository<Serie, UUID> {

    @Query("select distinct s from Serie s left join fetch s.capitulos")
    fun findAllConCapitulos() : List<Serie>

    @Query("select distinct s from Serie s left join fetch s.capitulos where s.id = :id")
    fun findByIdConCapitulos(id:UUID) : Optional<Serie>

}

interface CapituloRepository : JpaRepository<Capitulo, UUID> {

}

@Component
class InitDataComponent(
        val serieRepository: SerieRepository,
        val capituloRepository: CapituloRepository
) {

    @PostConstruct
    fun initData()  {
        val serie1 = Serie("El Mandaloriano", LocalDate.of(2019, 11, 12), Plataforma.DISNEY,1)
        serieRepository.save(serie1)

        val serie1capitulos = listOf(
                Capitulo("1x01 Capitulo Uno", LocalDate.of(2019, 11, 12), serie1),
                Capitulo("1x02 El niño", LocalDate.of(2019,11,15), serie1),
                Capitulo("1x03 El pecado", LocalDate.of(2019,11,22), serie1),
                Capitulo("1x04 El santuario", LocalDate.of(2019, 11, 29), serie1),
                Capitulo("1x05 El pistolero", LocalDate.of(2019, 12, 6), serie1),
                Capitulo("1x06 El prisionero", LocalDate.of(2019, 12, 13), serie1),
                Capitulo("1x07 Ajuste de cuentas", LocalDate.of(2019, 12, 18), serie1),
                Capitulo("1x08 Redención", LocalDate.of(2019, 12, 27), serie1)
        )

        capituloRepository.saveAll(serie1capitulos)


        val serie2 = Serie("The Witcher", LocalDate.of(2019, 12, 20), Plataforma.NETFLIX, 1)
        serieRepository.save(serie2)

        val fechaComun = LocalDate.of(2019, 12, 20)
        val serie2capitulos = listOf(
                Capitulo("1x01 Principio del fin", fechaComun, serie2),
                Capitulo("1x02 Cuatro marcos", fechaComun, serie2),
                Capitulo("1x03 Luna traicionera", fechaComun, serie2),
                Capitulo("1x04 Banquetes, bastardos y entierros", fechaComun, serie2),
                Capitulo("1x05 Apetitos incontenibles", fechaComun, serie2),
                Capitulo("1x06 Especies raras", fechaComun, serie2),
                Capitulo("1x07 Antes de la caída", fechaComun, serie2),
                Capitulo("1x08 Mucho más", fechaComun, serie2)
        )
        capituloRepository.saveAll(serie2capitulos)
    }

}