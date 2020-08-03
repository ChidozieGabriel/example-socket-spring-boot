package com.chidozie.socket.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

inline fun <reified T> String?.toObject(): T? {
    val gson = GsonBuilder()
        .setLenient()
        .create()
    return gson.fromJson(this, object : TypeToken<T>() {}.type)
}

inline fun <reified T> String?.toObjectOrNull(): T? {
    return try {
        toObject()
    } catch (e: Exception) {
        null
    }
}

inline fun <reified T> String?.toObjectNonNull(): T = toObject()!!

inline fun <reified T> Array<out Any>.toObjectNonNull(): T {
    val string = this[0].toString()
    return string.toObjectNonNull()
}

fun <T> T?.toJson(): String {
    return Gson().toJson(this, object : TypeToken<T>() {}.type)
}
