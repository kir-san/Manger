package com.san.kir.features.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.san.kir.features.viewer.databinding.OtherPageBinding
import kotlinx.coroutines.launch

internal abstract class TemplateFragment : Fragment() {
    private var _binding: OtherPageBinding? = null
    private val binding get() = _binding!!

    abstract val textRes: Int
    abstract val onClickListener: View.OnClickListener
    abstract val buttonVisibility: Boolean

    val viewModel: ViewerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = OtherPageBinding.inflate(inflater, container, false)

        binding.text.setText(textRes)

        binding.button.isVisible = buttonVisibility
        binding.button.setOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        binding.layout.setOnClickListener(onClickListener)
        binding.text.setOnClickListener(onClickListener)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

internal class NonePrevFragment : TemplateFragment() {
    override val textRes = R.string.viewer_page_none_prev_text
    override val buttonVisibility = false
    override val onClickListener = View.OnClickListener {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }
}

internal class PrevFragment : TemplateFragment() {
    override val textRes = R.string.viewer_page_prev_text
    override val buttonVisibility = true
    override val onClickListener = View.OnClickListener {
        lifecycleScope.launch {
            viewModel.chaptersManager.prevChapter()
        }
    }
}

internal class NextFragment : TemplateFragment() {
    override val textRes = R.string.viewer_page_next_text
    override val buttonVisibility = true
    override val onClickListener = View.OnClickListener {
        lifecycleScope.launch {
            viewModel.chaptersManager.nextChapter()
        }
    }
}

internal class NoneNextFragment : TemplateFragment() {
    override val textRes = R.string.viewer_page_none_next_text
    override val buttonVisibility = false
    override val onClickListener = View.OnClickListener {
        activity?.onBackPressedDispatcher?.onBackPressed()
    }
}
