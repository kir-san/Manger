package com.san.kir.features.viewer

import android.annotation.SuppressLint
import android.graphics.PointF
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.SCALE_TYPE_CUSTOM
import com.davemorrissey.labs.subscaleview.decoder.SkiaPooledImageRegionDecoder
import com.san.kir.features.viewer.databinding.PageBinding
import com.san.kir.features.viewer.utils.LoadState
import com.san.kir.features.viewer.utils.Page
import com.san.kir.features.viewer.utils.animate
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.Executors

@SuppressLint("ClickableViewAccessibility")
@AndroidEntryPoint
internal class PageFragment : Fragment() {
    companion object {
        private const val page_name = "page_name"

        fun newInstance(page: Page.Current): PageFragment {
            return PageFragment().apply {
                arguments = bundleOf(page_name to page)
            }
        }
    }

    private val viewModel: ViewerViewModel by activityViewModels()
    private val images: LoadImageViewModel by viewModels()

    private var _binding: PageBinding? = null
    private val binding get() = _binding!!

    private val eventListener = object : SubsamplingScaleImageView.DefaultOnImageEventListener() {
        override fun onReady() {
            Timber.v("onReady")
            // Установка зума и расположения страницы
            binding.viewer.setMinimumScaleType(SCALE_TYPE_CUSTOM)
            binding.viewer.minScale = binding.viewer.width / binding.viewer.sWidth.toFloat()
            binding.viewer.setScaleAndCenter(
                binding.viewer.minScale,
                PointF(binding.viewer.sWidth / 2f, 0f)
            )
        }

        override fun onImageLoaded() {
            Timber.v("onImageLoaded")
            binding.progress.isVisible = false
        }

        override fun onPreviewLoadError(e: Exception?) {
            Timber.v("onPreviewLoadError")
        }

        override fun onImageLoadError(e: Exception?) {
            Timber.v("onImageLoadError")
        }

        override fun onTileLoadError(e: Exception?) {
            Timber.v("onTileLoadError")
        }

        override fun onPreviewReleased() {
            Timber.v("onPreviewReleased")
        }
    }

    private val page: Page.Current? by lazy { arguments?.getParcelable(page_name) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = PageBinding.inflate(inflater, container, false)
        .apply { _binding = this }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gesture = createGesture { x -> viewModel.clickOnScreen(x) }

        // Настройка просмоторщика
        binding.viewer.setOnTouchListener { _, event -> gesture.onTouchEvent(event) }
        binding.viewer.setOnImageEventListener(eventListener)
        binding.viewer.setRegionDecoderFactory { SkiaPooledImageRegionDecoder() }
        binding.viewer.setExecutor(Executors.newCachedThreadPool())

        // Настройка кнопки обновления
        binding.update.setOnClickListener {
            images.setInitState()
            viewModel.updatePagesForChapter().invokeOnCompletion {
                images.load(page, true)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // Реакция на загрузку изображения
        images.state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .onEach { state ->
                when (state) {
                    is LoadState.Error -> {
                        binding.errorText.text = when (state.exception) {
                            is UnknownHostException -> getString(
                                R.string.error_host, state.exception.localizedMessage
                            )

                            is SocketTimeoutException, is SocketException -> getString(
                                R.string.error_socket_timeout, state.exception.localizedMessage
                            )

                            is ClientRequestException -> when (state.exception.response.status) {
                                HttpStatusCode.NotFound -> getString(R.string.error_not_found)

                                else -> getString(
                                    R.string.error_argument,
                                    state.exception.response.status.toString()
                                )
                            }

                            else ->
                                getString(R.string.error_argument, state.exception.localizedMessage)
                        }
                        binding.errorText.isVisible = true
                        binding.progress.isVisible = false
                        binding.progressText.isVisible = false
                    }

                    LoadState.Init -> {
                        binding.progress.isVisible = true
                        binding.progressText.isVisible = false
                        binding.errorText.isVisible = false
                    }

                    is LoadState.Load -> {
                        binding.progressText.isVisible = true
                        binding.progressText.text = "${(state.percent * 100).toInt()}%"
                    }

                    is LoadState.Ready -> {
                        binding.progressText.isVisible = false
                        binding.viewer.setImage(state.image)
                        viewModel.chaptersManager
                            .updateStatisticData(state.imageSize, state.downloadTime)
                    }
                }
            }.launchIn(lifecycleScope)

        // Изменение видимости кнопки
        viewModel
            .visibleUI
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .onEach { showUI(binding.update, it) }
            .launchIn(lifecycleScope)

        viewModel
            .hasScrollbars
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .onEach(binding.viewer::setScrollbarsVisible)
            .launchIn(lifecycleScope)

        images.load(page)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewer.setOnTouchListener(null)
        binding.viewer.setOnImageEventListener(null)
        binding.update.setOnClickListener(null)
        _binding = null
    }

    private fun showUI(view: View, state: Boolean) {
        if (state) {
            animate(
                onUpdate = { anim -> view.translationY = 200f - anim },
                onStart = { view.isVisible = true }
            )
        } else {
            animate(
                onUpdate = { anim -> view.translationY = anim },
                onEnd = { view.isVisible = false }
            )
        }
    }

    private fun createGesture(onTapListener: (x: Float) -> Unit): GestureDetectorCompat {
        val listener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                onTapListener(e.x)
                return true
            }
        }

        return GestureDetectorCompat(this.requireContext(), listener)
    }
}
