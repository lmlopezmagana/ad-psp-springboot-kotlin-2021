# Manejo de errores, validación y patrón DTO

Este ejemplo nos va a permitir profundizar en 3 conceptos diferentes, pero que vamos a utilizar de forma más o menos relacionada, en nuestra API Rest desarrollada con Spring Boot y Kotlin.

## 1. Manejo de errores

Antes de Spring 3.2, los dos enfoques principales para manejar excepciones en una aplicación Spring MVC eran `HandlerExceptionResolver` o la anotación `@ExceptionHandler` . Ambos tienen algunas desventajas claras.

Desde la versión 3.2, hemos tenido la anotación `@ControllerAdvice` (y `@RestControllerAdvice`) para abordar las limitaciones de las dos soluciones anteriores y promover un manejo unificado de excepciones en toda la aplicación.

Ahora Spring 5 presenta la clase `ResponseStatusException`, una forma rápida para el manejo básico de errores en nuestras API REST.

Todos ellos tienen una cosa en común: abordan muy bien la separación de preocupaciones. La aplicación puede generar excepciones normalmente para indicar una fallo de algún tipo, que luego se manejará por separado.

> Todos estos enfoques parten de la base de definir excepciones propias para el manejo de situaciones de error.


### 1.1 Enfoque con `@ExceptionHandler` en el controlador

La primera solución funciona a nivel de `@Controller`. Definiremos un método para manejar excepciones y lo anotaremos con `@ExceptionHandler`:

```kotlin
class FooController{
    
    //...
    @ExceptionHandler([CustomException1::class, CustomException2::class])
    fun handleException() {
        // devolver respuesta de error conveniente
    }
}
```

Este enfoque tiene el problema de que el tratamiento de errores que se realiza aplica solamente a este controlador. Si queremos que este tratamiento esté unificado en varios controladores, tendríamos que replicar el código en cada controlador, lo cual no es mantenible.

### 1.2 Enfoque con `@ControllerAdvice` o `@RestControllerAdvice`

Si queremos tener métodos anotados con `@ExceptionHandler` que nos permitan manejar errores de forma global a más de un controlador podemos definir estos métodos en una clase anotada con `@ControllerAdvice` (si se trata de una API Rest, con `@RestControllerAdvice`).

![Arquitectura del manejo de errores en una aplicación Spring](https://uploads.toptal.io/blog/image/123908/toptal-blog-image-1503383110049-1cd3d10e7706d202ceb2a844d63f7351.png)

Entonces, al usar `@ExceptionHandler` y `@RestControllerAdvice`, podremos definir un punto central para tratar las excepciones y envolverlas en un objeto de error conveniente con mejor organización que el mecanismo predeterminado de manejo de errores de Spring Boot.

Si bien podemos definir una clase cualquiera como `@ControllerAdvice` o `@RestControllerAdvice`, es cierto que Spring Boot nos ofrece una clase conveniente que extender, que es `ResponseEntityExceptionHandler`. Esta clase es una clase base conveniente para que podamos personalizar el manejo de errores. Ofrece métodos que podemos sobrescribir para tratar algunos de los errores más comunes.

```kotlin
@RestControllerAdvice
class GlobalRestControllerAdvice : ResponseEntityExceptionHandler() {

}
```

Retomaremos este código más adelante, pero antes vamos a ver cuál va a ser la forma en que vamos a _envolver_ nuestros mensajes de error para que la respuesta sea más conveniente.

## 2. Mensajes de error

El módulo Spring Framework MVC viene con algunas características excelentes para ayudar con el manejo de errores. Pero queda en manos del desarrollador usar esas funciones para tratar las excepciones y devolver respuestas significativas al cliente API.

Un mensaje de error tipo se enviamos una petición POST incorrecta, por ejemplo, en el tipo de dato de uno de los atributos sería este:

```json
{
 "timestamp": 1500597044204,
 "status": 400,
 "error": "Bad Request",
 "exception": "org.springframework.http.converter.HttpMessageNotReadableException",
 "message": "JSON parse error: Unrecognized token 'three': was expecting ('true', 'false' or 'null'); nested exception is com.fasterxml.jackson.core.JsonParseException: Unrecognized token 'aaa': was expecting ('true', 'false' or 'null')\n at [Source: java.io.PushbackInputStream@cba7ebc; line: 4, column: 17]",
 "path": "/birds"
}
```

Bueno ... el mensaje de respuesta tiene algunos campos buenos, pero se centra demasiado en cuál fue la excepción. Por cierto, esta es la clase `DefaultErrorAttributes` de Spring Boot. El campo `timestamp` es un número entero que ni siquiera contiene información de en qué unidad de medida se encuentra la marca de tiempo. El campo `exception` sólo es interesante para los desarrolladores de Java/Kotlin y el mensaje deja a los consumidores del API perdido en todos los detalles de implementación que son irrelevantes para ellos. ¿Y si hubiera más detalles que pudiéramos extraer de la excepción de la que se originó el error? Vamos a tratar de crear una representación JSON más agradable par nuestros errores para así facilitar la vida de nuestros clientes API.

```kotlin
data class ApiError(
    val estado: HttpStatus,
    val mensaje: String?,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val subErrores: List<out ApiSubError>? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy hh:mm:ss")
    val fecha: LocalDateTime = LocalDateTime.now(),
)
```

- El campo `estado` nos permitirá saber el estado de la respuesta (400 - BAD_REQUEST, por ejemplo)
- El campo `mensaje` puede contener un mensaje sencillo sobre el error.
- El campo `fecha` definirá una representación de la fecha más conveniente que el `toString` por defecto de `LocalDateTime`
- El campo `subErrores` contendrá una serie de errores secundarios que sucedieron. Un ejemplo sería los errores de validación, en el cual varios campos pueden no haber superado este proceso (por ejemplo, nos hemos equivocado dejando un campo en blanco, y un número no acotado dentro de un rango).


Podemos completar este código con el siguiente:

```kotlin
open abstract class ApiSubError

data class ApiValidationSubError(
    val objeto : String,
    val campo : String,
    val valorRechazado : Any?,
    val mensaje : String?
) : ApiSubError()
```

De esta forma, `ApiValidationSubError` es una clase que extiende de `ApiSubError` y que nos permite encapsular un error sucedido durante una validación.

A continuación podemos ver cuál podría ser una respuesta más o menos completa a una petición REST que produce un error `400`:

```json
{
    "estado": "BAD_REQUEST",
    "mensaje": "Error de validación",
    "subErrores": [
        {
            "objeto": "editCategoriaDto",
            "campo": "urlImagen",
            "valorRechazado": "asdfg",
            "mensaje": "debe ser un URL válido"
        },
        {
            "objeto": "editCategoriaDto",
            "campo": "nombre",
            "valorRechazado": "",
            "mensaje": "El nombre de la categoría no puede quedar vacío"
        }
    ],
    "fecha": "19/02/2021 09:03:31"
}
```

Vamos a unificar lo que hemos trabajado hasta ahora para poder tener mensajes de error como este en un tratamiento de errores unificado para toda nuestra API Rest.

## 3. Manejo de errores global con una respuesta conveniente

En el punto 1.2 nos quedamos con este fragmento de clase

```kotlin
@RestControllerAdvice
class GlobalRestControllerAdvice : ResponseEntityExceptionHandler() {

}
```

Decíamos que `ResponseEntityExceptionHandler` es una clase conveniente para extender y crear un _ControllerAdvice_. Si revisamos su documentación, vemos que tiene muchos métodos que podemos extender. El más interesante es `fun handleExceptionInternal(...) : ResponseEntity<Any>`. De él, dice la documentación: _A single place to customize the response body of all exception types_, un único lugar para personalizar el cuerpo de las respuestas de error para todos los tipos de excepciones.

Por tanto, podemos sobrescribir este método para utilizar la clase `ApiError` para encapsular nuestros mensajes de error; y esto daría cobertura a todas las excepciones generadas en nuestra api:

```kotlin
override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val apiError = ApiError(status, ex.message)
        return ResponseEntity.status(status).body(apiError)
    }
```

De esta forma, la respuesta de cualquier excepción provocada en el sistema tendrá el JSON de respuesta que nosotros buscábamos.

### 3.1 Manejo de excepciones específicas

Este tratamiento global no quita que podamos definir algunas de un tipo específico. Supongamos que queremos dar un tratamiento específico a la situación de error de buscar una entidad o lista de entidades, y no encontrar nada en nuestro repositorio. Lo normal sería devolver una respuesta 404. Podemos manejar esto en nuestro controlador/servicio lanzando una excepción específica de entre las siguientes:

```kotlin
open class EntityNotFoundException(val msg: String) : RuntimeException(msg)

data class SingleEntityNotFoundException(
    val id: String,
    val javaClass: Class<out Any>
) : EntityNotFoundException("No se puede encontrar la entidad de tipo ${javaClass.name} con ID: ${id}")

data class ListEntityNotFoundException(
    val javaClass: Class<out Any>
) : EntityNotFoundException("No se pueden encontrar elementos del tipo ${javaClass.name} para esa consulta")


```

Estas excepciones se podrían lanzar así:

```kotlin
    @GetMapping("/{id}")
    fun getById(@PathVariable id : Long) =
        categoriaRepository.findById(id)
            .orElseThrow {
                SingleEntityNotFoundException(id.toString(), Categoria::class.java)
            }

```

Para captura este tipo de excepciones, y manejarlas, añadiríamos este método a nuestro `GlobalRestControllerAdvice`:

```kotlin
@RestControllerAdvice
class GlobalRestControllerAdvice : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value=[EntityNotFoundException::class])
    fun handleNotFoundException(ex: EntityNotFoundException) =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiError(HttpStatus.NOT_FOUND, ex.message))

    // Resto del código
}
```

Nótese que este método no extiende a ninguno definido por `ResponseEntityExceptionHandler`, sino que es un método propio; está anotado con `@ExceptionHandler`, que recibe como argumento un array con las excepciones que _escuchará_ y tratará, dando respuesta.

Lo cual nos podría producir mensajes como este:

```json
{
    "estado": "NOT_FOUND",
    "mensaje": "No se pueden encontrar elementos del tipo com.salesianostriana.dam.ejerciciojpakotlin.Categoria para esa consulta",
    "fecha": "19/02/2021 09:13:26"
}
```

El mensaje de error se podría modificar a nuestra conveniencia, si se entiende que este no es el más adecuado.

## 4. Validación

La validación es un proceso mediante el cual podemos asegurar que los datos de entrada de una aplicación cumplen una serie de restricciones; y si no las cumplen, podemos informar al usuario (en nuestro caso, cliente de la API Rest) de los errores que ha cometido al proporcionarnos esos datos.

Spring Boot tiene como estándar de facto para validar a [Hibernate Validator](http://hibernate.org/validator/), la implementación de referencia de la especificación de [validación de beans](https://beanvalidation.org/).

En primer lugar, si queremos utilizar esta validación en nuestro proyecto, y estamos usando Spring Boot 2.3 o superior, tendremos que añadir la siguiente dependencia:

```gradle
	implementation("org.springframework.boot:spring-boot-starter-validation")
```

Este mecanismo de valiación nos permite utilizar anotaciones sobre las propiedades de nuestras clases (sean entidades o no) para que después puedan ser validadas conforme a dichas restricciones. Por ejemplo:

```kotlin
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

```

**Todavía hay partes de este código que tenemos que explicar, pero sí que nos podemos centrar en algunas de las anotaciones**.

Según la especificación de _Bean Validation_ para Java, tenemos disponibles algunas anotaciones como:

- `@NotNull`: el valor no puede ser nulo
- `@Min`, `@Max`: el valor debe ser como mínimo o máximo un valor especificado
- `@NotEmpty`: el valor no puede ser nulo y debe tener al menos un carácter que no sea un espacio en blanco.
- `@Past`: el valor debe ser una fecha anterior a la fecha actual
- ...

Puedes encontrar todas estas anotaciones en la documentación oficial: [https://beanvalidation.org/2.0/spec/#builtinconstraints](https://beanvalidation.org/2.0/spec/#builtinconstraints)

> **¡OJO!** Para usar todas estas anotaciones en Kotlin, debemos hacer uso de las anotaciones _use-site targets_. Este mecanismo de Kotlin es necesario porque, a diferencia de Java, no estamos declarando en cada clase un propiedad más un método *getter* y otro *setter*, sino que estos son de alguna manera generados para nosotros. Por tanto, al escribir la anotación, tenemos que indicar si la queremos asociar al campo (`field`) o al método *getter* (`get`), de forma que anotaciones como `@NotNull` quedarían como `@get:NotNull` o `@field:NotNull`. En el caso de las anotaciones de validación podemos usar tanto `get` como `field`, y en los ejemplos que podáis encontrar por la red se utilizan indistintamente. 

El mecanismo por defecto de validación de beans nos permite escribir un mensaje de _error de validación_ en la propia etiqueta, de forma que podría quedar así:

```kotlin
@Entity
class Producto(
    @get:NotBlank(message="El nombre del producto es obligatorio")
    var nombre: String,

    // resto del cuerpo de la clase
}
```

Sin embargo, esta estrategia es poco sostenible en el tiempo, y tampoco nos permite aplicar internacionalización a nuestro proyecto. Veremos más adelante como podemos definir los mensajes de error en un fichero externo.

Ahora, si queremos que los datos proporcionados por el usuario (cliente del api) sean válidos con respecto a estas restricciones, tenemos que utilizar la anotación `@Valid` junto al mecanismo mediante el cual estamos recibiendo estos datos (usualmente a través de `@RequestBody` en un API Rest):

```kotlin
RestController
@RequestMapping("/categoria")
class CategoriaController {

    // Resto del código

    @PostMapping
    fun create(@Valid @RequestBody nueva : EditCategoriaDto) = ...
 
    // Resto del código

}
```


### 4.1 Cómo informar de los errores de validación

Cuando Spring Boot encuentra un argumento anotado con `@Valid`, automáticamente arranca la implementación predeterminada de JSR 380 - Hibernate Validator - y valida el argumento. Cuando el argumento de destino no pasa la validación, Spring Boot lanza una excepción de tipo `MethodArgumentNotValidException`.

Precisamente, uno de los métodos que podemos sobrescribir en nuestra clase `GlobalRestControllerAdvice` que viene definido en la claes `ResponseEntityExceptionHandler` de la cual estamos heredando es `handleMethodArgumentNotValid`; es decir, este método nos permite manejar cómo informar de los errores de validación que se hayan producido.

La implementación de este método puede quedar así:

```kotlin
@RestControllerAdvice
class GlobalRestControllerAdvice : ResponseEntityExceptionHandler() {

    // Resto del código

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ) : ResponseEntity<Any> =
        ResponseEntity
            .status(status)
            .body(
                ApiError(
                    status,
                    "Error de validación (handleMethodArgumentNotValid)",
                    ex.fieldErrors.map {
                        ApiValidationSubError(it.objectName, it.field, it.rejectedValue, it.defaultMessage)
                    }
                )
            )

}
```

El método `handleMethodArgumentNotValid` recibe como argumentos:

- La excepción de tipo `MethodArgumentNotValidException`.
- Los encabezados de la petición en una instancia de tipo `HttpHeaders`.
- El estado de la respuesta
- La petición en sí encapsulada en un objeto de tipo `WebRequest`.

Nuestra idea de cómo va a ser el mensaje de respuesta al error lo dejamos claro en el apartado 2:

```json
```json
{
    "estado": "BAD_REQUEST",
    "mensaje": "Error de validación",
    "subErrores": [
        {
            "objeto": "editCategoriaDto",
            "campo": "urlImagen",
            "valorRechazado": "asdfg",
            "mensaje": "debe ser un URL válido"
        },
        {
            "objeto": "editCategoriaDto",
            "campo": "nombre",
            "valorRechazado": "",
            "mensaje": "El nombre de la categoría no puede quedar vacío"
        }
    ],
    "fecha": "19/02/2021 09:03:31"
}
```

Si revisamos la documentación de `MethodArgumentNotValidException`, podemos encontrar que tiene un método, llamado `fieldErrors`, que es una lista de objetos `FieldError`. Cada uno de estos objetos encapsula un error de validación, permitiéndonos obtener sobre qué objeto y campo del mismo ha sucedido el error, cuál es el valor rechazado y qué mensaje para informar al usuario tenemos que proporcionar. De esta forma, podemos transformar el objeto de tipo `MethodArgumentNotValidException` con su lista de `FieldError` en un `ApiError` con una lista de `ApiValidationSubError`.

### 4.2 Cómo separar los mensajes de error en un fichero independiente

En el apartado 4 decíamos que era poco manejable asociar el mensaje de error a la anotación de una forma directa, porque esto era difícilmente mantenible, y no permite que podamos tener ese mismo mensaje de error en diferentes idiomas.

Spring nos proporciona un mecanismo mediante el cuál podemos separar los mensajes de error en un fichero (de properties) independiente. Para ello tenemos que indicar:

- Que cargue dicho fichero en un bean específico
- Asociar ese bean específico como fuente de mensajes al mecanismo de validación por defecto.

Todo ello lo hacemos a través de una clase de configuración con dos beans.

```kotlin
@Configuration
class ConfiguracionValidacion {

    @Bean
    fun messageSource() : MessageSource {
        var messageSource = ReloadableResourceBundleMessageSource()

        messageSource.setBasename("classpath:messages")
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }


    @Bean
    fun getValidator() : LocalValidatorFactoryBean {
        val validator = LocalValidatorFactoryBean()
        validator.setValidationMessageSource(messageSource())
        return validator
    }

}
```

- El primer método, `messageSource`, indica que vamos a cargar un fichero, que se llama `messages` (sin indicar la extensión del fichero), y que se encuentra en algún sitio dentro del `classpath` (como por ejemplo puede ser la ruta `src/main/resources`), y que la codificación del contenido es UTF-8.
- El segundo método, `getValidator`, configura el validador por defecto asignándole como fuente de mensajes de validación (`setValidationMessageSource`) el bean anterior.

Podemos entonces crear un fichero, llamado `messages.properties`, que podría tener un contenido así:

```properties
categoria.nombre.blank=El nombre de la categoría no puede quedar vacío
producto.nombre.blank=El nombre del producto no puede quedar vacío
producto.precio.null=El precio del producto es un campo obligatorio
producto.precio.min=El precio del producto debe ser como mínimo 0
producto.categoria.null=Todo producto debe estar asociado a una categoría
```

En él podemos definir diferentes entradas, asociándole un mensaje de error. Una buena regla mnemotécnica para dar consistencia a los títulos de los mensajes de error puede ser `objeto.campo.restriccion`, como por ejemplo: `categoria.nombre.blank` o `producto.precio.min`.

> Este mecanismo es más potente de lo que se presenta aquí, ya que estos mensajes pueden recibir argumentos desde fuera, o se puede internacionalizar creando diferentes ficheros con el nombre `messages-lang.properties` donde `lang` es el código ISO del idioma (`messages-en.properties`, `messsages-fr.properties`, ...). Puedes encontrar un ejemplo más completo en [https://www.javadevjournal.com/spring-boot/spring-custom-validation-message-source/](https://www.javadevjournal.com/spring-boot/spring-custom-validation-message-source/)

De esta forma, cuando definimos nuestras anotaciones de validación, en lugar de escribir un mensaje literal, podemos hacer referencia a alguna de las entradas escritas en `message.properties`.

```kotlin
@Entity
class Producto(
    @get:NotBlank(message="{producto.nombre.blank}")
    var nombre: String,

    // resto del cuerpo de la clase
}
```

## 5. Data Transfer Object

Un _DTO_ (_Data Transfer Object_) es un objeto plano que nos permite transportar información entre diferentes procesos o capas de nuestra aplicación. Su existencia en el ámbito de nuestra api rest tiene sentido debido a que, **en la mayoría de las situaciones, las entidades no son buenos objetos para devolver como respuesta, o como entrada de datos a través de una petición**. Las entidades tienen como misión fundamental servirnos como puente con una base de datos relacional, representando en nuestra aplicación los conocidos como _business objects_, pero en muchas ocasiones no serán los mejores candidatos para devolver como respuesta a una petición.

Un ejemplo podría ser la siguiente entidad:

```kotlin
@Entity
class Categoria(
    @field:NotBlank(message="{categoria.nombre.blank}") var nombre: String,
    @get:URL var urlImagen: String,
    @ManyToOne var padre: Categoria? = null,
    @Id @GeneratedValue val id : Long? = null
) { ... }
```

Entre otras propiedades, esta entidad tiene una referencia de tipo `Categoria` (a sí misma) que indica que una categoría puede tener como padre a otra categoría. Sin embargo:

- Si utilizamos este modelo como respuesta a una petición, por ejemplo para obtener una categoría por su ID, podríamos tener una respuesta así:

```json
{
    id: 6,
    nombre: "Pan tostado"
    urlImagen: "http://...",
    padre: {
        id: 4,
        nombre: "Panes y bollería"
        urlImagen: "http://...",
        padre: {
            id: 3,
            nombre: "Alimentación seca"
            urlImagen: "http://....."
            padre: {
                id: 2,
                nombre: "Alimentación"
                urlImagen: "http://...."
                padre: null
            }
        }
    }
}
```

Esta cadena se puede tornar en una recursividad de objetos anidados muy grandes que posiblemente no nos interesen. 

- Si utilizamos este modelo como entrada de datos, para crear una nueva categoría, tendríamos la dificultad de dar el valor a la categoría padre. Spring espera que le proporcionemos todo un objeto Kotlin de tipo `Categoria`, pero nosotros, seguramente, queramos proporcionar esto:

```json
{
    nombre: "Pan tostado"
    urlImagen: "http://...."
    padre: 4
}
```

Por tanto, la clase `Categoria` no nos serviría para recoger la información del JSON anterior.

Por tanto, es más que habitual que para cada entidad de nuestra api rest tengamos que definir algunos DTOs para obtener valores o para transformar la salida a un JSON más conveniente.

> Spring y JacksonMapper ofrecen algunos mecanismos más que los DTOs para moldear el JSON de salida de una petición, pero no los explicaremos aquí.

Algunos DTOs útiles podrían ser estos:

```kotlin
data class EditCategoriaDto(
    @get:NotBlank(message="{categoria.nombre.blank}") var nombre: String,
    @get:URL var urlImagen: String,
    var padre: Long? = null
)

data class GetCategoriaDtoPadre(
    var id: Long?,
    var nombre: String?
)

data class GetCategoriaDto(
    var id: Long?,
    var nombre: String,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var urlImagen: String,

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var padre: GetCategoriaDtoPadre?
)


fun Categoria.toGetCategoriaDto() : GetCategoriaDto {
    var padreDto : GetCategoriaDtoPadre? = null
    if (padre != null)
        padreDto = GetCategoriaDtoPadre(padre!!.id, padre!!.nombre)
    return GetCategoriaDto(id, nombre, urlImagen, padreDto)
}


```

Si además añadimos una función de extensión que nos permita transformar la entidad `Categoria` en un DTO `GetCategoriaDto` el código de nuestro controlador podría quedar así:

```kotlin
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

    // Resto del código del controlador
}
```

Como hemos podido observar, los DTOs también pueden usar el mecanismo de validación de beans, y lo podríamos usar en la entrada de datos validados:

```kotlin
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

```


## 6. Conclusiones

En conclusión, en este amplio ejemplo hemos podido aprender:

- A gestionar las situaciones de error de nuestra API a través de excepciones propias
- A transformar esos errores en mensajes más adecuados a los clientes de nuestra API
- A validar los datos de entrada, proporcionando unos mensajes de error consistentes
- A separar los mensajes de error de los errores propiamente dicho, pudiendo además internacionalizar nuestra aplicación.
- A transformar los datos de nuestras entidades en objetos más apropiados como son los DTOs.



# Bibliografía

1. https://www.baeldung.com/exception-handling-for-rest-with-spring
2. https://www.toptal.com/java/spring-boot-rest-api-error-handling
3. https://www.baeldung.com/spring-boot-bean-validation