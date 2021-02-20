package com.salesianostriana.dam.imageupload.upload

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Service
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.ResponseErrorHandler
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import org.springframework.web.util.UriBuilderFactory
import java.net.URI
import java.util.*


@Service
class ImgurService(
        @Value("\${imgur.clientid}") val clientId: String

) {
    var restTemplate: RestTemplate = RestTemplate()

    init {
        restTemplate.errorHandler = object : DefaultResponseErrorHandler() {
            override fun hasError(response: ClientHttpResponse) =
                    response.statusCode.series() === HttpStatus.Series.CLIENT_ERROR || response.statusCode.series() === HttpStatus.Series.SERVER_ERROR


            override fun handleError(url: URI, method: HttpMethod, response: ClientHttpResponse) {
                when(response.statusCode) {
                    HttpStatus.INTERNAL_SERVER_ERROR -> throw RuntimeException("Error de servidor")
                    HttpStatus.BAD_REQUEST -> throw ImgurBadRequest()
                    HttpStatus.NOT_FOUND -> throw ImgurImageNotFoundException()
                }

            }

        }
    }

    private var factory: UriBuilderFactory = DefaultUriBuilderFactory()
    val logger: Logger = LoggerFactory.getLogger(ImgurService::class.java)
    companion object {
        private const val BASE_URL = "https://api.imgur.com/3/image"
        const val URL_NEW_IMAGE = "$BASE_URL"
        const val URL_DELETE_IMAGE = "$BASE_URL/{hash}"
        const val URL_GET_IMAGE = "$BASE_URL/{id}"
        const val SUCCESS_UPLOAD_STATUS = 200
        const val SUCCESS_GET_STATUS = 200
    }

    fun upload(imageReq: NewImageReq): Optional<NewImageRes> {

        var headers = getHeaders()

        //var restTemplate = RestTemplate()

        var request: HttpEntity<NewImageReq> = HttpEntity(imageReq, headers)

        var imageRes: NewImageRes? = restTemplate.postForObject(URL_NEW_IMAGE, request, NewImageRes::class.java)

        if (imageRes != null && imageRes.status == SUCCESS_UPLOAD_STATUS)
            return Optional.of(imageRes)
        return Optional.empty()

    }

    fun delete(hash: String): Unit {
        logger.debug("Realizando la petición DELETE para eliminar la imagen $hash")

        //var restTemplate = RestTemplate()

        var uri: URI = factory.uriString(URL_DELETE_IMAGE).build(hash)
        var request: RequestEntity<Void> = RequestEntity.delete(uri).headers(getHeaders()).build()

        restTemplate.exchange(request, Void::class.java)


    }

    fun get(id: String): Optional<GetImageRes> {

        //var restTemplate = RestTemplate()

        var uri: URI = factory.uriString(URL_GET_IMAGE).build(id)
        var request: RequestEntity<Void> = RequestEntity
                .get(uri)
                .headers(getHeaders())
                .accept(MediaType.APPLICATION_JSON)
                .build()

        var response = restTemplate.exchange(request, GetImageRes::class.java)


        if (response.statusCode.is2xxSuccessful && response.hasBody())
            return Optional.of(response.body as GetImageRes)
        return Optional.empty()
    }

    private fun getHeaders(): HttpHeaders {
        var headers: HttpHeaders = HttpHeaders()
        headers["Authorization"] = "Client-ID $clientId"
        headers.contentType = MediaType.APPLICATION_JSON
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        return headers
    }


}


class ImgurImageNotFoundException() : RuntimeException("No se ha podido encontrar la imagen")
class ImgurBadRequest() : RuntimeException("Error al realizar la petición")

@JsonIgnoreProperties(ignoreUnknown = false)
data class NewImageReq(
        var image: String,
        var name: String

)

@JsonIgnoreProperties(ignoreUnknown = false)
data class NewImageRes(
        val success: String,
        val status: Int,
        val data: NewImageInfo
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class NewImageInfo(
        val link: String,
        val id: String,
        val deletehash: String
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class GetImageRes(
        val success: String,
        val status: Int,
        val data: GetImageInfo
)

@JsonIgnoreProperties(ignoreUnknown = false)
data class GetImageInfo(
        val link: String,
        val id: String,
        val type: String
)