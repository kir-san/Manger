package com.san.kir.data.store

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.san.kir.manger.ShikimoriAuth
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
internal object ShikimoriAuthSerializer : Serializer<ShikimoriAuth> {
    override val defaultValue: ShikimoriAuth = ShikimoriAuth.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ShikimoriAuth {
        try {
            return ShikimoriAuth.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: ShikimoriAuth, output: OutputStream) {
        t.writeTo(output)
    }
}
