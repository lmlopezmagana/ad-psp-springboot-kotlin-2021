package com.salesianostriana.dam.ejerciciojpakotlin

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class InitDataComponent {

    @Autowired
    lateinit var productoRepository : ProductoRepository

    @Autowired
    lateinit var categoriaRepository : CategoriaRepository


    @PostConstruct
    fun initData() {

        var c = Categoria("Jamón del bueno", "http://...")
        categoriaRepository.save(c)

        var p1 = Producto("Jamón ibérico de 7kg", 300.0, "lalalalalalalalal", "http://....", c)
        productoRepository.save(p1)




    }


}


