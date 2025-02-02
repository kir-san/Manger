package com.san.kir.data.parsing

import com.san.kir.core.internet.ConnectManager
import com.san.kir.core.utils.asHttp
import com.san.kir.core.utils.asHttps
import com.san.kir.data.models.catalog.SiteCatalogElement
import com.san.kir.data.models.main.Chapter
import com.san.kir.data.models.main.Manga
import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

public abstract class SiteCatalog {
    public abstract val name: String
    public abstract val catalogName: String

    public open val host: String
        get() = "http://$catalogName"
    public open val allCatalogName: List<String>
        get() = listOf(catalogName)

    public open val headers: StringValues? = null
    public open val hasPopulateSort: Boolean = true
    internal open val servers: List<String> = emptyList()

    internal abstract val catalog: String

    public abstract var volume: Int

    public abstract suspend fun init(): SiteCatalog

    internal open suspend fun fullElement(element: SiteCatalogElement): SiteCatalogElement = element
    public abstract fun catalog(): Flow<SiteCatalogElement>
    internal abstract suspend fun elementByUrl(url: String): SiteCatalogElement?
    internal abstract suspend fun chapters(manga: Manga): List<Chapter>
    internal abstract suspend fun pages(item: Chapter): List<String>
}

internal abstract class SiteCatalogClassic : SiteCatalog()

public abstract class SiteCatalogAlternative : SiteCatalog()

internal fun SiteCatalog.getShortLink(fullLink: String): String {
    val foundedCatalogs = allCatalogName
        .filter { catalog ->
            fullLink.startsWith(catalog.asHttp(), true) || fullLink.startsWith(catalog.asHttps(), true)
        }

    val shortLink: String
    if (foundedCatalogs.size == 1 || fullLink.contains(catalogName, true)) {
        shortLink = fullLink.split(foundedCatalogs.first()).last()
    } else {
        Timber.v("fullLink = $fullLink")
        Timber.v("foundedCatalogs = $foundedCatalogs")
        throw Throwable("Каталогов найдено больше одного или не найдено совсем")
    }

    return shortLink
}

public data class LoginAvatar(val login: String, val avatar: String)

public interface SiteConstants {
    public val AUTH_URL: String
    public val HOST_NAME: String
    public val SITE_NAME: String
    public suspend fun User(connectManager: ConnectManager): LoginAvatar? {
        return null
    }
}
