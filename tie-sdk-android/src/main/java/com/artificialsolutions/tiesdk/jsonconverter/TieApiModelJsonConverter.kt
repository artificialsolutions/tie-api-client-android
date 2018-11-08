package com.artificialsolutions.tiesdk.jsonconverter

import com.artificialsolutions.tiesdk.model.TieCloseSessionResponse
import com.artificialsolutions.tiesdk.model.TieResponse
import com.google.gson.GsonBuilder;
import retrofit2.converter.gson.GsonConverterFactory

internal class TieApiModelJsonConverter {
    companion object {
        internal fun buildGsonConverter(): GsonConverterFactory {
            val gsonBuilder = GsonBuilder();

            gsonBuilder.registerTypeAdapter(TieResponse::class.java, OutputModelDeserializer());
            gsonBuilder.registerTypeAdapter(TieCloseSessionResponse::class.java, CloseModelDeserializer());
            val myGson = gsonBuilder.create();

            return GsonConverterFactory.create(myGson);
        }
    }
}