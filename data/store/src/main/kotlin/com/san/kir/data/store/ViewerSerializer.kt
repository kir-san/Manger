package com.san.kir.data.store

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.san.kir.manger.Viewer
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
internal object ViewerSerializer : Serializer<Viewer> {
    override val defaultValue: Viewer = Viewer.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Viewer {
        try {
            return Viewer.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Viewer, output: OutputStream) {
        t.writeTo(output)
    }
}
