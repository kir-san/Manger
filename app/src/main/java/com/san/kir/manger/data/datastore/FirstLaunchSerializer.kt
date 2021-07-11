package com.san.kir.manger.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.san.kir.manger.FirstLaunch
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
object FirstLaunchSerializer : Serializer<FirstLaunch> {
    override val defaultValue: FirstLaunch = FirstLaunch.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): FirstLaunch {
        try {
            return FirstLaunch.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: FirstLaunch, output: OutputStream) {
        t.writeTo(output)
    }
}
