package com.artificialsolutions.tiesdk.jsonconverter

import com.artificialsolutions.tiesdk.exception.TieApiException
import com.artificialsolutions.tiesdk.model.TieErrorResponse
import com.artificialsolutions.tiesdk.model.TieResponse
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import java.lang.reflect.Type


internal class OutputModelDeserializer: JsonDeserializer<TieResponse> {
    val outputModelRequiredFields: Array<String> = arrayOf("input", "output", "status", "sessionId")
    val errorModelRequiredFields: Array<String> = arrayOf("message")

    @Throws(JsonParseException::class, TieApiException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TieResponse {
        val gson = Gson()

        if (isValidOutputModel(json.asJsonObject, outputModelRequiredFields)) {
            try {
                val output = gson.fromJson<TieResponse>(json, TieResponse::class.java)

                return output
            } catch (ex: JsonParseException) {}
        }

        if (isValidOutputModel(json.asJsonObject, errorModelRequiredFields)) {
            try {
                val errorMessage = gson.fromJson<TieErrorResponse>(json, TieErrorResponse::class.java)

                throw TieApiException(errorMessage)
            } catch (ex: JsonParseException) {
                throw ex
            }
        }

        throw JsonParseException("Could not parse data.")
    }

    private fun isValidOutputModel(jsonObject: JsonObject, requiredFields: Array<String>): Boolean {
        for (fieldName in requiredFields) {
            if (jsonObject.get(fieldName) == null) {
                return false
            }
        }
        return true
    }
}

