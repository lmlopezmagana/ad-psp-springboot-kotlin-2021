package com.salesianostriana.dam.ejerciciojpakotlin

import org.springframework.data.jpa.repository.JpaRepository

interface CategoriaRepository : JpaRepository<Categoria, Long>

interface ProductoRepository : JpaRepository<Producto, Long>

interface UsuarioRepository : JpaRepository<Usuario, Long>

interface ValoracionRepository : JpaRepository<Valoracion, ValoracionPK>