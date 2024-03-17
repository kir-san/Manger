package com.san.kir.core.utils

import android.app.Application
import android.content.Context
import com.arkivanov.decompose.extensions.compose.jetpack.stack.animation.StackAnimator
import com.san.kir.core.utils.navigation.NavComponent
import com.san.kir.core.utils.navigation.NavConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import kotlin.reflect.KClass

object ManualDI {
    const val TAG: String = "MANUAL DI"

    private var app: Application? = null
    private val navigationCreators: MutableMap<KClass<NavConfig>, (NavConfig) -> NavComponent<*>> =
        hashMapOf()
    private val navigationAnimations: MutableMap<KClass<NavConfig>, (NavConfig) -> StackAnimator> =
        hashMapOf()

    fun init(application: Application) {
        app = application
    }

    val context: Context
        get() = requireNotNull(app) { "call init before use" }

    val application: Application
        get() = requireNotNull(app) { "call init before use" }

    @OptIn(ExperimentalSerializationApi::class)
    val json: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }
    }

    fun addNavigationCreator(key: KClass<NavConfig>, creator: (NavConfig) -> NavComponent<*>) {
        navigationCreators[key] = creator
    }

    internal fun navComponent(config: NavConfig): NavComponent<*>? {
        return navigationCreators[config::class]?.invoke(config)
    }

    fun addNavigationAnimation(key: KClass<NavConfig>, creator: (NavConfig) -> StackAnimator) {
        navigationAnimations[key] = creator
    }

    internal fun navAnimation(config: NavConfig): StackAnimator? {
        return navigationAnimations[config::class]?.invoke(config)
    }

    inline fun <reified Data> jsonToString(data: Data): String {
        return json.encodeToString(data)
    }

    inline fun <reified Data> stringToJson(data: String): Data? {
        return runCatching {
            json.decodeFromString<Data>(data)
        }.onFailure {
            Timber.tag(TAG).e(it)
        }.getOrNull()
    }
}
