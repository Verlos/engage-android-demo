package com.proximipro.engagesdkdemo.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.proximipro.engage.android.Engage
import com.proximipro.engage.android.util.getEngage
import com.proximipro.engagesdkdemo.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Controller for splash screen
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val extras = intent?.extras
        if (extras != null && extras.containsKey("notification_id")) {
            startActivity(Intent(this, MainActivity::class.java).apply {
                extras.keySet()?.forEach {
                    putExtra(it, extras.getString(it))
                }
            })
            finish()
            return
        }
        createDelay()
        Engage.isLoggingEnabled = true
    }

    private fun createDelay() {
        GlobalScope.launch {
            delay(1000)
            withContext(Dispatchers.Main) {
                proceedNext()
            }
        }
    }

    private fun proceedNext() {
        when {
            !Engage.isInitialized -> navigateToWelcomeScreen()
            getEngage().isUserRegistered -> navigateToMainScreen()
            else -> navigateToRegisterScreen()
        }
    }

    private fun navigateToRegisterScreen() {
        startActivity(Intent(this, RegistrationActivity::class.java))
        finish()
    }

    private fun navigateToWelcomeScreen(shouldAddError: Boolean = false) {
        startActivity(Intent(this, WelcomeActivity::class.java).apply {
            if (shouldAddError) {
                putExtra("message", "Unable to verify Api Key")
            }
        })
        finish()
    }

    private fun navigateToMainScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
