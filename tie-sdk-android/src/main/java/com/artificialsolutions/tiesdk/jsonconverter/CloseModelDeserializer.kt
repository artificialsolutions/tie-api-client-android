package com.artificialsolutions.tiesdk.jsonconverter

import com.artificialsolutions.tiesdk.exception.TieApiException
import com.artificialsolutions.tiesdk.model.TieCloseSessionResponse
import com.artificialsolutions.tiesdk.model.TieErrorResponse
import com.google.gson.*
import java.lang.reflect.Type

internal class CloseModelDeserializer: JsonDeserializer<TieCloseSessionResponse> {
    val closeModelRequiredFields: Array<String> = arrayOf("response")
    val errorModelRequiredFields: Array<String> = arrayOf("message")

    @Throws(JsonParseException::class, TieApiException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TieCloseSessionResponse {
        val gson = Gson()

        if (isValidOutputModel(json.asJsonObject, closeModelRequiredFields)) {
            try {
                val output = gson.fromJson<TieCloseSessionResponse>(json, TieCloseSessionResponse::class.java)

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

