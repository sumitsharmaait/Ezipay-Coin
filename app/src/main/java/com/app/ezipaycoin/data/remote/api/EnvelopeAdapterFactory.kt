package com.app.ezipaycoin.data.remote.api

import com.app.ezipaycoin.data.remote.dto.response.BaseResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

/**
 * The backend answers a refused request with `apiStatus: false` and sometimes puts a plain string
 * (or another shape) in `apiData`. The typed model cannot hold that, so the parse used to throw.
 *
 * This reads the envelope normally first. Only when that fails and `apiStatus` is false does it
 * retry with `apiData` set to null, so the caller still gets `apiMessage` and `error`.
 * Successful responses go through the default adapter and are parsed exactly as before.
 */
class EnvelopeAdapterFactory : TypeAdapterFactory {
    @Suppress("UNCHECKED_CAST")
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType != BaseResponse::class.java) return null
        val delegate = gson.getDelegateAdapter(this, type)
        val tree = gson.getAdapter(com.google.gson.JsonElement::class.java)
        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T) = delegate.write(out, value)

            override fun read(reader: JsonReader): T? {
                val element = tree.read(reader)
                return try {
                    delegate.fromJsonTree(element)
                } catch (e: JsonParseException) {
                    val obj = element as? JsonObject ?: throw e
                    val status = obj.get("apiStatus")
                    val refused = status != null && status.isJsonPrimitive &&
                        status.asJsonPrimitive.isBoolean && !status.asBoolean
                    if (!refused) throw e
                    val copy = obj.deepCopy()
                    copy.add("apiData", JsonNull.INSTANCE)
                    delegate.fromJsonTree(copy)
                }
            }
        }
    }

    companion object {
        val gson: Gson by lazy { GsonBuilder().registerTypeAdapterFactory(EnvelopeAdapterFactory()).create() }
    }
}
