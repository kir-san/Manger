package com.san.kir.ui.viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.san.kir.ui.viewer.databinding.OtherPageBinding

abstract class TemplateFragment : Fragment() {
    private var _binding: OtherPageBinding? = null
    private val binding get() = _binding!!

    abstract val textRes: Int
    abstract val onClickListener: View.OnClickListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = OtherPageBinding.inflate(inflater, container, false)

        binding.text.setText(textRes)

        binding.button.setOnClickListener {
            activity?.onBackPressed()
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

class NonePrevFragment : TemplateFragment() {
    override val textRes = R.string.viewer_page_none_prev_text
    override val onClickListener = View.OnClickListener {

    }
}

class PrevFragment : TemplateFragment() {
    override val textRes = R.string.viewer_page_prev_text
    override val onClickListener = View.OnClickListener {

    }
}

class NextFragment : TemplateFragment() {
    override val textRes = R.string.viewer_page_next_text
    override val onClickListener = View.OnClickListener {

    }
}

class NoneNextFragment : TemplateFragment() {
    override val textRes = R.string.viewer_page_none_next_text
    override val onClickListener = View.OnClickListener {

    }
}
