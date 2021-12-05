package com.san.kir.ui.viewer

import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.san.kir.ui.viewer.databinding.OtherPageBinding
import com.san.kir.ui.viewer.databinding.PageBinding
import com.san.kir.ui.viewer.subsampling.SubsamplingScaleImageView
import com.san.kir.ui.viewer.subsampling.SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP

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
    private val listener by lazy { Listener(viewModel) }
    private val gesture by lazy { GestureDetectorCompat(this.requireContext(), listener) }

    private var _binding: PageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = PageBinding.inflate(inflater, container, false)

        binding.viewer.setMinimumScaleType(SCALE_TYPE_CENTER_CROP)
        binding.viewer.setOnTouchListener { _, event -> gesture.onTouchEvent(event) }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewer.setOnTouchListener(null)
        _binding = null
    }
}

internal class Listener(viewModel: ViewerViewModel) : GestureDetector.SimpleOnGestureListener() {
    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {

        return true
    }
}
