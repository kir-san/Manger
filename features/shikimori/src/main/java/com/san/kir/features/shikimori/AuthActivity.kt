package com.san.kir.features.shikimori

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.san.kir.core.utils.log
import dagger.hilt.android.AndroidEntryPoint

object ShikimoriAuth {
    fun start(
        context: Context,
    ) {
        AuthActivity.start(context)
    }
}

@AndroidEntryPoint
internal class AuthActivity : ComponentActivity() {
    companion object {
        fun start(
            context: Context,
        ) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = ShikimoriData.authorizeUrl
            context.startActivity(intent)
        }
    }

    private val viewModel: ShikimoriAuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val code = intent.data?.getQueryParameter("code")
        log("code is $code")
        if (code != null) {
            viewModel.login(code).invokeOnCompletion {
                finish()
            }
        } else finish()
    }
}
