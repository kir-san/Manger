package com.san.kir.ui.viewer

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
import androidx.lifecycle.lifecycleScope
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP
import com.san.kir.core.utils.coroutines.withMainContext
import com.san.kir.core.utils.log
import com.san.kir.ui.viewer.databinding.PageBinding
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch

internal class PageFragment : Fragment(), FlowCollector<LoadState> {
    companion object {
        private const val page_name = "page_name"

        fun newInstance(page: Page.Current): PageFragment {
            return PageFragment().apply {
                arguments = bundleOf(page_name to page)
            }
        }
    }

    private val viewModel: ViewerViewModel by activityViewModels()

    private var _binding: PageBinding? = null
    private val binding get() = _binding!!

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val left = viewModel.screenParts.value.first
            val right = viewModel.screenParts.value.second
            val x = e.x

            // Включен ли режим управления нажатиями на экран
            if (viewModel.control.value.taps) {
                if (x < left) { // Нажатие на левую часть экрана
                    viewModel.chaptersManager.prevPage() // Предыдущая страница
                } else if (x > right) { // Нажатие на правую часть
                    viewModel.chaptersManager.nextPage() // Следущая страница
                }
            }

            // Если нажатие по центральной части
            if (e.x > left && e.x < right) {
                viewModel.toogleVisibilityUI() // Переключение видимости баров
            }

            return true
        }
    }

    private val eventListener = object : SubsamplingScaleImageView.DefaultOnImageEventListener() {
        override fun onReady() {
            log("onReady")
            binding.progress.isVisible = false
        }

        override fun onImageLoaded() {
            log("onImageLoaded")
        }

        override fun onPreviewLoadError(e: Exception?) {
            log("onPreviewLoadError")
        }

        override fun onImageLoadError(e: Exception?) {
            log("onImageLoadError")
        }

        override fun onTileLoadError(e: Exception?) {
            log("onTileLoadError")
        }

        override fun onPreviewReleased() {
            log("onPreviewReleased")
        }
    }

    private val gesture by lazy { GestureDetectorCompat(this.requireContext(), gestureListener) }
    private val page: Page.Current? by lazy { arguments?.getParcelable(page_name) }
    private var job: Job? = null

    @OptIn(InternalCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = PageBinding.inflate(inflater, container, false)

        job = lifecycleScope.launchWhenResumed {
            viewModel.loadImage(page).collect(this@PageFragment)
        }

        binding.viewer.setMinimumScaleType(SCALE_TYPE_CENTER_CROP)
        binding.viewer.setOnTouchListener { _, event -> gesture.onTouchEvent(event) }
        binding.viewer.setOnImageEventListener(eventListener)

        binding.update.setOnClickListener {
            job?.cancel()
            job = lifecycleScope.launch { viewModel.loadImage(page).collect(this@PageFragment) }
        }

        return binding.root
    }

    override suspend fun emit(value: LoadState) = withMainContext {
        when (value) {
            is LoadState.Error -> TODO()
            LoadState.Init -> {
                binding.progress.isIndeterminate = true
                binding.progress.isVisible = true
            }
            is LoadState.Load -> {
                binding.progress.isIndeterminate = false
                binding.progress.isVisible = true
                binding.progress.progress = (value.percent * 100).toInt()
            }
            is LoadState.Ready -> {
                binding.viewer.setImage(value.image)
                binding.progress.isVisible = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewer.setOnTouchListener(null)
        binding.viewer.setOnImageEventListener(null)
        binding.update.setOnClickListener(null)
        _binding = null
    }
}
