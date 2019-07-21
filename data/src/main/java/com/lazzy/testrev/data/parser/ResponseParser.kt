package com.lazzy.testrev.data.parser

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.lazzy.testrev.data.dataobjects.CurrentCourse
import java.lang.reflect.Type


class ResponseParser : JsonDeserializer<CurrentCourse> {

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): CurrentCourse {
        val `object` = json?.asJsonObject ?: throw JsonParseException("Json is null")
        val rates = `object`.getAsJsonObject("rates")
        val currencies = mutableMapOf<String, Double>()
        for (entry in rates.entrySet()) {
            currencies[entry.key] = entry.value.asDouble
        }

        return CurrentCourse(
            `object`.getAsJsonPrimitive("base")
                .asString,
            `object`.getAsJsonPrimitive("date")
                .asString,
            currencies
        )
    }
}