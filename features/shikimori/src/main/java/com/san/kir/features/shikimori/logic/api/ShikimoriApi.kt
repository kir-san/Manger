package com.san.kir.features.shikimori.logic.api

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/api")
class ShikimoriApi {

    @Serializable
    @Resource("users")
    class Users(val parent: ShikimoriApi = ShikimoriApi()) {

        @Serializable
        @Resource("whoami")
        class Whoami(val parent: Users = Users())
    }

    @Serializable
    @Resource("mangas")
    class Mangas(
        val parent: ShikimoriApi = ShikimoriApi(),
        val search: String = "",
        val limit: Int = 50,
        val order: String = "name",
    ) {
        @Serializable
        @Resource("{id}")
        class Id(val parent: Mangas = Mangas(), val id: Long)
    }

    @Serializable
    @Resource("v2")
    class V2(val parent: ShikimoriApi = ShikimoriApi()) {

        @Serializable
        @Resource("user_rates")
        class UserRates(
            val parent: V2 = V2(),
            val user_id: Long? = null,
            val target_id: Long? = null,
            val target_type: String? = null
        ) {

            @Serializable
            @Resource("{id}")
            class Id(val parent: UserRates = UserRates(), val id: Long)
        }
    }
}

