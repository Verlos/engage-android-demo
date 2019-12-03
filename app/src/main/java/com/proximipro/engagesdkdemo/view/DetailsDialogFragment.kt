package com.proximipro.engagesdkdemo.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.proximipro.engage.android.model.remote.Content
import com.proximipro.engagesdkdemo.R
import com.proximipro.engagesdkdemo.helper.Constants
import kotlinx.android.synthetic.main.frag_details.contentDetail
import kotlinx.android.synthetic.main.frag_details.ivClose
import kotlinx.android.synthetic.main.frag_details.tvTitle

/**
 * Shows details screen for the loaded content when clicked
 */
class DetailsDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "DetailsDialogFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.NotificationDialogAnimation
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        super.onCreateDialog(savedInstanceState).apply {
            window?.apply {
                val width = ViewGroup.LayoutParams.MATCH_PARENT
                val height = ViewGroup.LayoutParams.MATCH_PARENT
                setLayout(width, height)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ivClose.setOnClickListener { dismiss() }
        val content = arguments?.getParcelable<Content>(Constants.BUNDLE_KEY_CONTENT) ?: return
        setContent(content)
    }

    private fun setContent(content: Content) {
        contentDetail.setContent(content)
        tvTitle.text = content.title
    }
}
