package com.san.kir.features.shikimori

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.features.shikimori.logic.api.ShikimoriData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

internal val Context.authCodeStore by preferencesDataStore(name = "shikimori")
internal val CODE = stringPreferencesKey("auth_code")

@AndroidEntryPoint
internal class AuthActivity : ComponentActivity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = ShikimoriData.authorizeUrl
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val code = intent.data?.getQueryParameter("code")
        Timber.v("code is $code")
        if (code != null) {
            lifecycleScope.launch {
                authCodeStore.edit { settings ->
                    settings[CODE] = code
                }
                withMainContext {
                    finish()
                }
            }
        } else finish()
    }
}
