package com.proximipro.engagesdkdemo.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.proximipro.engage.android.Engage
import com.proximipro.engage.android.util.Gender
import com.proximipro.engage.android.util.getEngage
import com.proximipro.engagesdkdemo.R
import com.proximipro.engagesdkdemo.helper.showSnackBar
import kotlinx.android.synthetic.main.activity_registration.btnSignUp
import kotlinx.android.synthetic.main.activity_registration.genderGroup
import kotlinx.android.synthetic.main.activity_registration.root
import kotlinx.android.synthetic.main.activity_registration.tvBirthDate
import java.util.Calendar
import java.util.Date

/**
 * Registration screen controller
 * @property selectedDate Date? is the selected birth date
 * @property engage Engage is the sdk instance
 */
class RegistrationActivity : AppCompatActivity() {

    companion object {
        private const val PROXIMIPRO_TERMS_AND_CONDITION_URL =
            "https://www.proximipro.com/terms-and-conditions"
    }

    private var selectedDate: Date? = null
    val engage: Engage = getEngage()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
    }

    /**
     * Shows a date picker dialog
     * @param v View is the clicked view
     */
    fun openDatePicker(v: View) {
        DatePickerDialogFragment { _, year, month, dayOfMonth ->
            onDateSet(year, month + 1, dayOfMonth)
        }.show(supportFragmentManager, "DatePicker")
    }

    private fun onDateSet(year: Int, month: Int, dayOfMonth: Int) {
        tvBirthDate?.text = "$year/$month/$dayOfMonth"
        selectedDate = Calendar.getInstance().let {
            it.set(year, month, dayOfMonth)
            it.time
        }
    }

    /**
     * Starts sign-up process
     * @param v View is the clicked view
     */
    fun signUp(v: View) {
        val date = selectedDate
        if (date == null) {
            showMessage(getString(R.string.select_birth_date))
            return
        }
        if (genderGroup.checkedRadioButtonId == -1) {
            showMessage(getString(R.string.select_gender))
            return
        }
        val gender =
            if (genderGroup.checkedRadioButtonId == R.id.rbMale) Gender.Male else Gender.Female
        registerUser(date, gender)
    }

    private fun registerUser(birthDate: Date, gender: Gender) {
        btnSignUp.startAnimation()
        engage.registerUser(
            birthDate = birthDate,
            gender = gender
        ) onSuccess {
            onSuccessfulRegistration()
        } onFailure {
            showMessage(it.message.toString())
            btnSignUp.revertAnimation()
        }
    }

    private fun onSuccessfulRegistration() {
        btnSignUp.doneLoadingAnimation(
            ContextCompat.getColor(this, R.color.colorAccent),
            BitmapFactory.decodeResource(resources, R.drawable.ic_done)
        )
        Handler().postDelayed({ navigateToTagsSelectionScreen() }, 500)
    }

    private fun navigateToTagsSelectionScreen() {
        startActivity(Intent(this, TagsSelectionActivity::class.java))
        finish()
    }

    /**
     * Opens terms & conditions page in browser
     * @param v View is the clicked view
     */
    fun openTermsAndConditions(v: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PROXIMIPRO_TERMS_AND_CONDITION_URL)))
    }

    private fun showMessage(msg: String) {
        showSnackBar(root, msg)
    }
}
