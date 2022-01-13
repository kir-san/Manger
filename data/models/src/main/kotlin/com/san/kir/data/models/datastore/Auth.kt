package com.san.kir.data.models.datastore

data class ShikimoriAuth(
    val isLogin: Boolean = false,
    val token: Token = Token(),
    val whoami: Whoami = Whoami(),
) {
    data class Token(
        val access_token: String = "",
        val token_type: String = "",
        val expires_in: Long = 0,
        val refresh_token: String = "",
        val scope: String = "",
        val created_at: Long = 0,
    ) {
        val isExpired: Boolean
            get() = (System.currentTimeMillis() / 1000) > (created_at + expires_in - 3600)
    }

    data class Whoami(
        val id: Long = 0,
        val nickname: String = "",
        val avatar: String = "",
    )
}
