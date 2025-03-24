package com.san.kir.data.parsing.sites

import com.san.kir.core.internet.ConnectManager
import com.san.kir.data.parsing.LoginAvatar
import com.san.kir.data.parsing.SiteConstants
import org.jsoup.nodes.Document
import timber.log.Timber
import java.util.regex.Pattern

public object AllhentaiConstants : SiteConstants {
    override val SITE_NAME: String = "All Hentai"
    override val HOST_NAME: String = "20.allhen.online"
    override val AUTH_URL: String = "$HOST_NAME/internal/auth"

    override suspend fun User(connectManager: ConnectManager): LoginAvatar? {
        val document = connectManager.getDocument(HOST_NAME)
        val doc = document.select(".account-menu")
        return LoginAvatar(
            login = doc.select("#accountMenu > div > div:nth-child(3)").first()?.text() ?: return null,
            avatar = document
                .select(".header-item.dropdown.user-profile-settings-link > a > span > img")
                .attr("src")
        )
    }
}

internal class Allhentai(connectManager: ConnectManager) : ReadmangaTemplate(connectManager) {
    override val name = AllhentaiConstants.SITE_NAME
    override val catalogName = AllhentaiConstants.HOST_NAME
    override var volume = 0

    override val allCatalogName: List<String>
        get() = super.allCatalogName + "allhentai.ru" + "23.allhen.online" + "22.allhen.online" +
                "2023.allhen.online"

    override val servers: List<String>
        get() = listOf("d.aaa200.rocks")

    override val categories = listOf(
        "3D",
        "Анимация",
        "Без текста",
        "Порно комикс",
        "Порно манхва"
    )

    override fun checkAuthorization(document: Document): Boolean {
        val text = document.select(".container .auth-page .alert").text()
        return "нужно авторизоваться!" in text
    }


    override suspend fun documentForPages(shortLink: String): Document {
        val doc = super.documentForPages(shortLink)
        val pat = Pattern.compile("(?:window\\.user_hash\\s*=\\s*'|[?&]d=)([a-zA-Z0-9]+)").matcher(doc.html())
        var userHashes = mutableListOf<String?>()
        while (pat.find()) {
            userHashes.add(pat.group(1))
            Timber.v("pat: ${pat.group(1)}")
        }
        val hash = userHashes.filterNotNull().random()
        return connectManager.getDocument("$host$shortLink?d=$hash", ignoreNotFound = true)
    }
}
