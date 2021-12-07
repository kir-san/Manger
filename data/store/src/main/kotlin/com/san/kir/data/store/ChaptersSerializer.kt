package com.san.kir.data.store

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.san.kir.manger.Chapters
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
internal object ChaptersSerializer : Serializer<Chapters> {
    override val defaultValue: Chapters = Chapters.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Chapters {
        try {
            return Chapters.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Chapters, output: OutputStream) {
        t.writeTo(output)
    }
}
