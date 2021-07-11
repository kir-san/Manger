package com.san.kir.manger.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.san.kir.manger.Main
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
object MainSerializer : Serializer<Main> {
    override val defaultValue: Main = Main.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Main {
        try {
            return Main.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Main, output: OutputStream) {
        t.writeTo(output)
    }
}
