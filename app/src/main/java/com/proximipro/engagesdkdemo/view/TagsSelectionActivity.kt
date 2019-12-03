package com.proximipro.engagesdkdemo.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.proximipro.engage.android.Engage
import com.proximipro.engage.android.util.getEngage
import com.proximipro.engagesdkdemo.R
import com.proximipro.engagesdkdemo.helper.Constants
import com.proximipro.engagesdkdemo.helper.showSnackBar
import kotlinx.android.synthetic.main.activity_tags_selection.btnCancel
import kotlinx.android.synthetic.main.activity_tags_selection.btnImDone
import kotlinx.android.synthetic.main.activity_tags_selection.progress
import kotlinx.android.synthetic.main.activity_tags_selection.root
import kotlinx.android.synthetic.main.activity_tags_selection.rvTags

class TagsSelectionActivity : AppCompatActivity() {

    private val engage: Engage by lazy { getEngage() }

    private val adapter = TagsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tags_selection)

        if (intent?.getBooleanExtra(Constants.INTENT_EXTRA_EDIT_TAGS, false) == true) {
            btnCancel.visibility = View.VISIBLE
        }
        rvTags.adapter = adapter
        adapter.setTags(engage.config().userConfig.getTags())
    }

    fun saveChanges(v: View? = null) {
        enableViews(false)
        saveTags()
    }

    private fun enableViews(enable: Boolean) {
        btnImDone.isEnabled = enable
        btnCancel.isEnabled = enable
        progress.visibility = if (enable) View.GONE else View.VISIBLE
    }

    private fun saveTags() {
        engage.updateUser(tags = adapter.tags) onSuccess {
            navigateToHomeScreen()
        } onFailure {
            showSnackBar(root, "Something went wrong!")
            enableViews(true)
        }
    }

    private fun navigateToHomeScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
