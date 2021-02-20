package com.salesianostriana.dam.imageupload.upload

import java.lang.StringBuilder
import javax.persistence.AttributeConverter
import javax.persistence.Converter


data class ImgurImageAttribute(var id: String?, var deletehash : String?)


@Converter(autoApply = true)
class ImgurImageAttributeToStringConverter() : AttributeConverter<ImgurImageAttribute, String?> {

    companion object {
        private const val SEPARATOR = ", "
    }

    override fun convertToDatabaseColumn(attribute: ImgurImageAttribute?): String? {
        if (attribute == null) return null
        return attribute.id + SEPARATOR + attribute.deletehash
    }

    override fun convertToEntityAttribute(dbData: String?): ImgurImageAttribute? {
        if (dbData == null) return null

        var pieces = dbData.split(SEPARATOR)

        if (pieces.isEmpty())
            return null

        var first = if (pieces[0].isNotEmpty()) pieces[0] else null
        var second: String? = if (pieces.size >= 2 && pieces[1].isNotEmpty()) pieces[1] else null

        return ImgurImageAttribute(first, second)
    }

}