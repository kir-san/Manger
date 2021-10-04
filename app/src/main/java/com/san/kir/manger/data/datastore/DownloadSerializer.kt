package com.san.kir.manger.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.san.kir.manger.Download
import com.san.kir.manger.Main
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
object DownloadSerializer : Serializer<Download> {
    override val defaultValue: Download = Download.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Download {
        try {
            return Download.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Download, output: OutputStream) {
        t.writeTo(output)
    }
}
