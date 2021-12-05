package com.san.kir.ui.viewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.san.kir.ui.viewer.databinding.MainBinding
import dagger.hilt.android.AndroidEntryPoint

object MangaViewer {
    fun start(
        context: Context,
        chapterID: Long,
    ) {
        ViewerActivity.start(context, chapterID)
    }
}

@AndroidEntryPoint
internal class ViewerActivity : AppCompatActivity() {

    companion object {
        private const val chapterKey = "chapter_key"

        fun start(
            context: Context,
            chapterID: Long,
        ) {
            val intent = Intent(context, ViewerActivity::class.java)

            intent.putExtra(chapterKey, chapterID)

            context.startActivity(intent)
        }
    }

    private val binding by lazy { MainBinding.inflate(layoutInflater) }
    private val viewModel: ViewerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }
}
