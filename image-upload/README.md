
# Ejemplo de subida de imágenes a imgur con Spring Boot y Kotlin

Este proyecto incluye todo lo necesario para poder incluir en tu Api REST con
 Spring Boot y Kotlin la subida de imágenes a imgur.
 
> Imgur es un servicio de imágenes que proporciona la posibilidad de subir imágenes de forma gratuita siempre que se realice desde una aplicación sin ánimo de lucro.

## ¿Cómo integrarlo con mi aplicación?

### Imgur Client-ID

Lo primero que te hace falta es registrar tu aplicación en imgur para obtener un `Client-ID`. Puedes seguir los pasos descritos [aquí](https://apidocs.imgur.com/?version=latest).

Cuando lo obtengas, tendrás asignarlo a la variable de entorno `IMGUR_CLIENTID`, y el api se encargará de establecerlo allá donde le haga falta.

Linux y Mac
```bash
export IMGUR_CLIENTID=Valor
```

Windows:
```bash
setx IMGUR_CLIENTID Valor
```

### Integración con nuestras entidades

Para poder integrar la subida de imágenes con nuestras entidades, tenemos disponible el tipo `ImgurImageAttribute`:

```kotlin
@Entity
data class Entidad(
        var texto : String,
        var img : ImgurImageAttribute? = null,
        @Id @GeneratedValue val id: Long? = null
)
```

Este tipo no necesita ninguna anotación, puesto que tiene implementado un `AttributeConverter`, que _traduce_ toda la información necesaria desde/hacia un `String`.

La clase `ImgurImageAttribute` ofrece dos propiedades:

- `id`: identificador de la imagen. Será necesario para cargar la misma.
- `deletehash`: hash de eliminación de la imagen. Se usa para su borrado.


### Controlador de obtención de imágenes.

Se proporciona un controlador, en la ruta `/files/{id}`, que permite obtener la imagen asociada a un ID. Por ejemplo, si el `id` de una imagen es `4mtegMx` la petición sería:

```bash
curl --location --request GET 'localhost:9000/files/4mtegMx'
```

### Servicio `ImgurStorageService` 

Esta clase es el _corazón_ de las subida de ficheros, ofreciendo entre otros los siguientes métodos:

```kotlin
override fun store(file: MultipartFile) : Optional<ImgurImageAttribute>
override fun delete(deletehash: String) : Unit
```

- El método `store` nos permite almacenar una imagen a partir de una instancia de tipo `MultipartFile`. Nos devuelve un `Optional<ImgurImageAttribute>` para poder guardarlo junto a nuestro modelo de datos.
- El método `delete` ofrece la funcionalidad de eliminar una imagen de Imgur.

### Ejemplo de uso:



```kotlin
@RestController
@RequestMapping("/entity/")
class UploadController(
        private val servicio: EntidadServicio
) {

    // Resto del código

    @GetMapping("/")
    fun getAll() : List<EntidadDto> {
        val result = servicio.findAll()
        if (result.isEmpty())
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No hay registros")
        return result.map { it.toDto() }

    }


    @PostMapping("/")
    fun create(@RequestPart("nuevo") new : NuevaEntidadDto, @RequestPart("file") file: MultipartFile ) : ResponseEntity<EntidadDto> {

        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(servicio.save(new.toEntidad(), file).toDto())
        } catch ( ex : ImgurBadRequest) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Error en la subida de la imagen")
        }

    }
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

}
```

