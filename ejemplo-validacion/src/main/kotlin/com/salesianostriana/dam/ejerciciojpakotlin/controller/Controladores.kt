package com.salesianostriana.dam.ejerciciojpakotlin

import com.salesianostriana.dam.ejerciciojpakotlin.data.EditCategoriaDto
import com.salesianostriana.dam.ejerciciojpakotlin.data.toGetCategoriaDetalleDto
import com.salesianostriana.dam.ejerciciojpakotlin.data.toGetCategoriaDto
import com.salesianostriana.dam.ejerciciojpakotlin.error.ListEntityNotFoundException
import com.salesianostriana.dam.ejerciciojpakotlin.error.SingleEntityNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/categoria")
class CategoriaController {

    @Autowired
    lateinit var categoriaRepository: CategoriaRepository

    @GetMapping
    fun getAll()  =
        categoriaRepository.findAll()
                .map { it.toGetCategoriaDto() }
                .takeIf { it.isNotEmpty() } ?:
                throw ListEntityNotFoundException(Categoria::class.java)


    @GetMapping("/{id}")
    fun getById(@PathVariable id : Long) =
        categoriaRepository.findById(id)
            .map { it.toGetCategoriaDetalleDto() }
            .orElseThrow {
                SingleEntityNotFoundException(id.toString(), Categoria::class.java)
            }


    @PostMapping
    fun create(@Valid @RequestBody nueva : EditCategoriaDto) =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(
                categoriaRepository.save(Categoria(
                                            nueva.nombre,
                                            nueva.urlImagen,
                                            nueva.padre?.let { categoriaRepository.findByIdOrNull(nueva.padre) }
                                        )
            ).toGetCategoriaDetalleDto())




    /**
     * En este método utilizamos un DTO para editar la categoria.
     * ¿La razón? La entidad tiene una referencia, padre, de tipo Categoria
     * (para modelar la jerarquía de categorías). Sin embargo, al editar,
     * le proporcionamos el ID de la categoría padre.
     * La validación basada en fichero de properties nos permite centralizar
     * el manejo de los mensajes de error.
     */
    @PutMapping("/{id}")
    fun edit(@Valid @RequestBody editada : EditCategoriaDto, @PathVariable id: Long) =
        categoriaRepository.findById(id)
            .map { fromRepo ->
                fromRepo.nombre = editada.nombre
                fromRepo.urlImagen = editada.urlImagen
                fromRepo.padre = editada.padre?.let { categoriaRepository.findByIdOrNull(editada.padre) }
                categoriaRepository.save(fromRepo).toGetCategoriaDetalleDto()
            }
            .orElseThrow { SingleEntityNotFoundException(id.toString(), Categoria::class.java) }


    @DeleteMapping("/{id}")
    fun delete (@PathVariable id: Long) : ResponseEntity<Any> {
        if (categoriaRepository.existsById(id))
            categoriaRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }


}

