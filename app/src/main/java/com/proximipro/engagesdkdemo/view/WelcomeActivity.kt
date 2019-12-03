package com.proximipro.engagesdkdemo.view

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.proximipro.engage.android.core.InitializationRequest
import com.proximipro.engage.android.util.InitializationCallback
import com.proximipro.engage.android.util.initializeEngage
import com.proximipro.engagesdkdemo.BuildConfig
import com.proximipro.engagesdkdemo.R
import com.proximipro.engagesdkdemo.helper.Constants
import com.proximipro.engagesdkdemo.helper.showSnackBar
import kotlinx.android.synthetic.main.activity_welcome.btnSubmit
import kotlinx.android.synthetic.main.activity_welcome.etApiKey
import kotlinx.android.synthetic.main.activity_welcome.root
import kotlinx.android.synthetic.main.activity_welcome.tvTryNow

/**
 * Controller for welcome screen
 * @property request InitializationRequest is the initialization request for the engage sdk
 * @property snackBar Snackbar? used to display alerts
 */
class WelcomeActivity : AppCompatActivity() {

    companion object {
        private const val PLATFORM_OPTIONS_URL = "https://www.proximipro.com"
        private const val DEMO_API_KEY = "DiDJ-FNba-kPN5-jYG2"
        private val apiKeys = arrayOf(
            "DiDJ-FNba-kPN5-jYG2",
            "URbF-jNZM-9UrR-Iib1",
            "5dW2-RYSJ-UDXT-P3qA",
            "jZa5-O80m-qJmT-r0oY"
        )
    }

    private val request: InitializationRequest by lazy {
        InitializationRequest(
            apiKey = DEMO_API_KEY,
            appName = getString(R.string.app_name),
            clientId = Constants.CLIENT_ID,
            regionId = Constants.REGION_IDENTIFIER,
            uuid = Constants.beaconUUID
        )
    }

    private var snackBar: Snackbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        etApiKey.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitApiKey()
                true
            } else false
        }

        intent?.apply {
            if (hasExtra("message")) {
                this.getStringExtra("message")?.let { message ->
                    showMessage(message)
                }
            }
        }

        if (BuildConfig.DEBUG && BuildConfig.FLAVOR == "development") {
            etApiKey.setOnClickListener {
                val position = apiKeys.indexOf(etApiKey.text.toString()).takeIf { it >= 0 } ?: 0
                AlertDialog.Builder(this)
                    .setSingleChoiceItems(apiKeys, position) { dialog, which ->
                        dialog.dismiss()
                        val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                        etApiKey.setText(apiKeys[selectedPosition])
                    }.show()
            }
        }
    }

    /**
     * Opens browser to load platform options web page
     * @param v View is the clicked view
     */
    fun openPlatformOptions(v: View) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLATFORM_OPTIONS_URL)))
    }

    /**
     * Initiates demo account process
     * @param v View
     */
    fun openDemoAccount(v: View) {
        tvTryNow.text = getString(R.string.loading)
        initializeEngage(
            applicationContext = application,
            request = request,
            callback = object : InitializationCallback() {
                override fun onSuccess() {
                    tvTryNow.text = getString(R.string.try_now)
                    navigateToRegistrationScreen()
                }

                override fun onError(e: Throwable) {
                    tvTryNow.text = getString(R.string.try_now)
                    showMessage(getString(R.string.unable_to_initialize))
                }
            })
    }

    private fun navigateToHomeScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    /**
     * Verifies user entered api key to the server and proceeds further
     * @param v View?
     */
    fun submitApiKey(v: View? = null) {
        val apiKey = etApiKey.text.toString()
        if (apiKey.isBlank() || apiKey == "null") {
            showMessage(getString(R.string.please_enter_valid_api_key))
            return
        }
        verifyApiKey(apiKey)
    }

    private fun verifyApiKey(apiKey: String) {
        btnSubmit.startAnimation()
        initializeEngage(
            applicationContext = application,
            request = request.copy(apiKey = apiKey),
            callback = object : InitializationCallback() {
                override fun onSuccess() {
                    onInitialized()
                }

                override fun onError(e: Throwable) {
                    showMessage(e.message.toString())
                    btnSubmit.revertAnimation()
                }
            })
    }

    private fun onInitialized() {
        btnSubmit.doneLoadingAnimation(
            ContextCompat.getColor(this, R.color.colorAccent),
            BitmapFactory.decodeResource(resources, R.drawable.ic_done)
        )
        Handler().postDelayed({
            navigateToRegistrationScreen()
        }, 500)
    }

    private fun navigateToRegistrationScreen() {
        startActivity(Intent(this, RegistrationActivity::class.java))
        finish()
    }

    private fun showMessage(msg: String) {
        snackBar = showSnackBar(root, msg)
    }
}
