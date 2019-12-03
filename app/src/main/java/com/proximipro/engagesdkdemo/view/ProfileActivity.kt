package com.proximipro.engagesdkdemo.view

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.proximipro.engage.android.Engage
import com.proximipro.engage.android.util.Gender
import com.proximipro.engage.android.util.getEngage
import com.proximipro.engagesdkdemo.R
import com.proximipro.engagesdkdemo.helper.showSnackBar
import kotlinx.android.synthetic.main.activity_profile.btnSaveChanges
import kotlinx.android.synthetic.main.activity_profile.ivClose
import kotlinx.android.synthetic.main.activity_profile.ivEdit
import kotlinx.android.synthetic.main.activity_profile.rbFemale
import kotlinx.android.synthetic.main.activity_profile.rbMale
import kotlinx.android.synthetic.main.activity_profile.root
import kotlinx.android.synthetic.main.activity_profile.rvTags
import kotlinx.android.synthetic.main.activity_profile.tvBirthDate
import timber.log.Timber
import java.util.Calendar
import java.util.Date

class ProfileActivity : AppCompatActivity() {

    private val engage: Engage by lazy {
        getEngage()
    }

    private var selectedDate: Date? = null
    private val adapter = TagsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        ivClose.setOnClickListener { finish() }
        ivEdit.setOnClickListener { enableViews(true) }

        tvBirthDate.text = engage.config().userConfig.birthDate
        engage.config().userConfig.gender.let {
            when (Gender.fromValue(it)) {
                is Gender.Male -> {
                    rbMale.isChecked = true
                }
                is Gender.Female -> {
                    rbFemale.isChecked = true
                }
            }
        }
        rvTags.adapter = adapter
        adapter.setTags(engage.config().userConfig.getTags())
        adapter.enabled = false
    }

    fun openDatePicker(v: View) {
        DatePickerDialog(this).apply {
            this.datePicker.maxDate =
                Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time.time
            setOnDateSetListener { _, year, month, dayOfMonth ->
                onDateSet(year, month, dayOfMonth)
            }
            show()
        }
    }

    private fun onDateSet(year: Int, month: Int, dayOfMonth: Int) {
        tvBirthDate.text = "$year/$month/$dayOfMonth"
        selectedDate = Calendar.getInstance().let {
            it.set(year, month, dayOfMonth)
            it.time
        }
    }

    private fun enableViews(enable: Boolean) {
        tvBirthDate.isEnabled = enable
        rbMale.isEnabled = enable
        rbFemale.isEnabled = enable
        btnSaveChanges.visibility = if (enable) View.VISIBLE else View.INVISIBLE
        adapter.enabled = enable
    }

    fun saveProfileChanges(v: View) {
        val gender = if (rbMale.isSelected) Gender.Male else Gender.Female
        btnSaveChanges.startAnimation()
        engage.updateUser(
            gender = gender,
            birthDate = selectedDate,
            tags = adapter.tags
        ) onSuccess {
            btnSaveChanges.doneLoadingAnimation(
                ContextCompat.getColor(this@ProfileActivity, R.color.colorAccent),
                BitmapFactory.decodeResource(resources, R.drawable.ic_done)
            )
            btnSaveChanges.revertAnimation()
            enableViews(false)
            showSnackBar(root, "Saved successfully")
        } onFailure {
            Timber.e(it)
            btnSaveChanges.revertAnimation()
            showSnackBar(root, "Something went wrong!")
        }
    }
}
