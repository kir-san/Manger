package com.san.kir.data.parsing.sites

import com.san.kir.core.internet.ConnectManager
import io.ktor.util.StringValues
import io.ktor.util.StringValuesBuilderImpl

internal class Mangachan(connectManager: ConnectManager) : MangachanTemplate(connectManager) {

    override val host: String
        get() = "https://$catalogName"

    override val name: String = SITE_NAME
    override val catalogName: String = HOST_NAME

    override var volume = 0

    override val headers: StringValues
        get() = StringValuesBuilderImpl(true, 1).apply {
            append("Referer", host)
        }.build()
    override val allCatalogName: List<String>
        get() = super.allCatalogName + "mangachan.ru" + "mangachan.me" + "manga-chan.me"

    companion object {
        const val SITE_NAME = "Манга - тян"
        const val HOST_NAME = "im.manga-chan.me"
    }
}
