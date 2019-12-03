package com.proximipro.engagesdkdemo.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.RemoteMessage
import com.proximipro.engage.android.Engage
import com.proximipro.engage.android.core.BeaconScanResultListener
import com.proximipro.engage.android.core.locationErrorLiveData
import com.proximipro.engage.android.locationCheckLiveData
import com.proximipro.engage.android.model.ProBeacon
import com.proximipro.engage.android.model.common.Action
import com.proximipro.engage.android.model.common.Rule
import com.proximipro.engage.android.model.remote.Content
import com.proximipro.engage.android.util.LogEventType
import com.proximipro.engage.android.util.NotificationData
import com.proximipro.engage.android.util.getEngage
import com.proximipro.engagesdkdemo.R
import com.proximipro.engagesdkdemo.helper.Constants
import com.proximipro.engagesdkdemo.helper.showSnackBar
import com.proximipro.engagesdkdemo.logger.LogCollector
import kotlinx.android.synthetic.main.activity_main.list
import kotlinx.android.synthetic.main.activity_main.llReportProgress
import kotlinx.android.synthetic.main.activity_main.progressBar
import kotlinx.android.synthetic.main.activity_main.root
import kotlinx.android.synthetic.main.activity_main.scanButton
import kotlinx.android.synthetic.main.activity_main.swipeRefresh
import kotlinx.android.synthetic.main.activity_main.tvLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.URLConnection
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Demo app Home screen that displays loaded content for the sdk
 * @property engage Engage is the sdk instance
 * @property beaconPair Pair<ProBeacon, String>? holds current beacon information
 * @property apiService ApiService is the main api service for making network calls to fetch content
 */
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val engage: Engage by lazy { getEngage() }
    private var beaconPair: Pair<ProBeacon, String>? = null
    private var triggeredRule: Rule? = null
    private var refreshJob = SupervisorJob()
    private var refreshScope = CoroutineScope(Dispatchers.IO + refreshJob)
    private val refreshLiveData = MutableLiveData<Boolean>()
    private val pref: SharedPreferences by lazy {
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    private val logCollector: LogCollector by lazy {
        LogCollector()
    }

    private val appPref: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val firebasePref: SharedPreferences by lazy {
        getSharedPreferences(Constants.PREF_FIREBASE, Context.MODE_PRIVATE)
    }

    private val pushReceiver = PushMessageBroadcastReceiver()

    companion object {
        private const val SETTINGS_REQUEST_CODE = 486
        private const val COARSE_LOCATION_PERMISSION =
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        private const val FINE_LOCATION_PERMISSION =
            android.Manifest.permission.ACCESS_FINE_LOCATION
        private const val PERMISSION_REQUEST_CODE = 777
        private const val SETTINGS_ACTIVITY_REQUEST_CODE = 659
        private const val PREF_NAME = "engage_location_pref"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        syncToken()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            pushReceiver,
            IntentFilter(Constants.PUSH_BROADCAST_INTENT_FILTER)
        )

        list.setItemClickListener {
            showDetailsScreen(it)
        }

        engage.config().pendingIntentClassName = MainActivity::class.java.name

        NotificationData.from(intent)?.let { data ->
            val (rule, action) = data
            engage.recordNotificationTapEvent(rule)
            engage.getContentForActions(
                listOf(action),
                success = {
                    it.firstOrNull()?.let(::showDetailsScreen)
                }, failure = ::onErrorLoadingContent
            )
        }

        swipeRefresh.setOnRefreshListener {
            beaconPair?.run { loadContentFrom(first) } ?: run {
                swipeRefresh.isRefreshing = false
            }
        }

        scanButton.setOnClickListener { initPermissionModel() }

        refreshLiveData.observe(this) {
            beaconPair?.run { loadContentFrom(first) }
        }

        if (appPref.getBoolean(SettingsFragment.AUTO_REFRESH, false)) {
            startAutoRefresh()
        }
        locationCheckLiveData.observe(this) {
            it?.run {
                tvLocation.visibility = View.VISIBLE
                tvLocation.text = "$latitude, $longitude"
            }
        }
        syncUIState()

        locationErrorLiveData.observe(this) { error ->
            //            error?.let(Crashlytics::logException)
        }
        val extras = intent?.extras
        if (extras != null) {
            extras.keySet()?.map {
                it to extras[it].toString()
            }?.toMap()?.let {
                logPushOpenEvent(it["notification_id"] ?: "")
                loadPushContent(it["url"].toString())
            }
        }
    }

    private fun showDetailsScreen(content: Content) {
        logTapEvent(content)
        DetailsDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelable(Constants.BUNDLE_KEY_CONTENT, content)
            }
        }.show(supportFragmentManager, DetailsDialogFragment.TAG)
    }

    private fun logTapEvent(content: Content) {
        beaconPair?.first?.let {
            engage.logContentTapEvent(LogEventType.Details, content, it).onFailure(Timber::e)
        } ?: triggeredRule?.let {
            engage.logContentTapEvent(LogEventType.Details, content, it).onFailure(Timber::e)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val (rule, action) = NotificationData.from(intent) ?: return
        engage.recordNotificationTapEvent(rule)
        engage.getContentForActions(
            listOf(action),
            success = {
                it.firstOrNull()?.let(::showDetailsScreen)
            }, failure = ::onErrorLoadingContent
        )
    }

    private suspend fun createReportFile(context: Context, reportFile: File): Uri {
        logCollector.dumpLog(reportFile)
        return FileProvider.getUriForFile(
            context,
            "com.proximipro.engagesdkdemo.FileProvider",
            reportFile
        )
    }

    private fun syncUIState() {
        if (engage.isScanOnGoing()) {
            scanButton.isActivated = true
            scanButton.text = getString(R.string.stop_scan)
            progressBar.visibility = View.VISIBLE
        } else {
            scanButton.text = getString(R.string.start_scan)
            scanButton.isActivated = false
            progressBar.visibility = View.GONE
            tvLocation.visibility = View.GONE
        }
    }

    private fun syncToken() {
        if (!firebasePref.getBoolean(Constants.PREF_KEY_TOKEN_IS_SYNCED, false)) {
            val token = firebasePref.getString(Constants.PREF_KEY_TOKEN, "") ?: return
            engage.registerPushToken(token).onSuccess {
                Timber.e("Token synced successfully")
            }.onFailure(Timber::e)
        }
    }

    private fun loadContentFrom(beacon: ProBeacon) {
        engage.getContentLoader().fetchContent(beacon) onSuccess {
            onNewContentFound(it)
        } onFailure (::onErrorLoadingContent)
    }

    override fun onDestroy() {
        super.onDestroy()
        refreshJob.cancel()
    }

    private fun loadContentFrom(action: Action) =
        engage.getContentForActions(
            listOf(action),
            success = ::onNewContentFound, failure = ::onErrorLoadingContent
        )

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.optionInfo)?.isVisible = beaconPair != null
        menu?.findItem(R.id.optionProfile)?.isVisible = engage.isUserRegistered
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.optionLogout -> showLogOutDialog()
            R.id.optionSettings -> openSettingsScreen()
            R.id.optionInfo -> showBeaconInfoDialog()
            R.id.optionProfile -> startActivity(Intent(this, ProfileActivity::class.java))
            R.id.optionSendReport -> sendScanReport()
        }
        return true
    }

    private fun sendScanReport() {
        llReportProgress.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.Default) {
            runCatching {
                val file =
                    File(this@MainActivity.filesDir, "log ${Date(System.currentTimeMillis())}.html")
                val reportFileUri =
                    createReportFile(this@MainActivity, file)
                withContext(Dispatchers.Main) {
                    llReportProgress.visibility = View.GONE
                    Timber.e("Opening share dialog for scan report")
                    ShareCompat.IntentBuilder.from(this@MainActivity)
                        .setStream(reportFileUri)
                        .setType(URLConnection.guessContentTypeFromName(file.name))
                        .startChooser()
                }
            }.onFailure {
                Timber.e("Error generating report")
                it.printStackTrace()
                withContext(Dispatchers.Main) {
                    llReportProgress.visibility = View.GONE
                }
            }
        }
    }

    private fun showLogOutDialog() =
        AlertDialog.Builder(this)
            .setTitle("Log out?")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ -> logOut() }
            .setNegativeButton("No") { _, _ -> }
            .takeIf { !isFinishing }?.show()

    private fun showBeaconInfoDialog() =
        BeaconDetailsDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelable("beacon", beaconPair?.first)
                putString("event", beaconPair?.second)
            }
        }.show(supportFragmentManager, BeaconDetailsDialogFragment.TAG)

    private fun openSettingsScreen() =
        startActivityForResult(Intent(this, SettingsActivity::class.java), SETTINGS_REQUEST_CODE)

    @SuppressLint("ApplySharedPref")
    private fun logOut() {
        GlobalScope.launch {
            FirebaseInstanceId.getInstance().deleteInstanceId()
        }
        getSharedPreferences(Constants.DEFAULT_PREF, Context.MODE_PRIVATE)?.edit()?.clear()
            ?.commit()
        engage.logout()
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }

    private fun startScan() {
        if (!engage.isScanOnGoing()) {
            scanButton.isActivated = true
            scanButton.text = getString(R.string.stop_scan)
            progressBar.visibility = View.VISIBLE
            startScanning()
        } else {
            stopScanning()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() =
        engage.startScan(this, object : BeaconScanResultListener() {
            override fun onLocationZoneEntered(rule: Rule, location: Location) {
                triggeredRule = rule
                engage.getContentLoader().fetchContent(location) onSuccess {
                    onNewContentFound(it)
                } onFailure (::onErrorLoadingContent)
            }

            override fun onRuleTriggered(rule: Rule) {
                triggeredRule = rule
                Timber.e("onRuleTriggered for foreground scan $rule")
            }

            override fun onBeaconExit(beacon: ProBeacon) {
                Timber.e("onBeaconExit: $beacon")
                beaconPair = Pair(beacon, "exit")
                invalidateOptionsMenu()
                loadContentFrom(beacon)
            }

            override fun onBeaconCamped(beacon: ProBeacon) {
                Timber.e("onBeaconCamped: $beacon")
                beaconPair = Pair(beacon, "enter")
                invalidateOptionsMenu()
                loadContentFrom(beacon)
            }

            override fun onScanStopped() {
                onContentScanStopped()
            }

        })

    private fun onContentScanStopped() {
        scanButton.isActivated = false
        scanButton.text = getString(R.string.start_scan)
        progressBar.visibility = View.GONE
        tvLocation.visibility = View.GONE
        swipeRefresh.isRefreshing = false
        beaconPair = null
        invalidateOptionsMenu()
    }

    private fun onErrorLoadingContent(throwable: Throwable) {
        Timber.e(throwable)
        swipeRefresh.isRefreshing = false
        beaconPair = null
        invalidateOptionsMenu()
        showSnackBar(root, "Unable to load content!")
        list.setContent(arrayListOf())
    }

    private fun onNewContentFound(contents: List<Content>) {
        list.setContent(contents)
        swipeRefresh.isRefreshing = false
    }

    private fun stopScanning() {
        engage.stopScan()
        stopAutoRefresh()
    }

    /**
     * Initiates permission model to request location permission.
     *
     * It follows google's recommendations for permission model.
     *
     * 1. Check whether the permission is already granted or not.
     * 2. If not, then check if rationale should be displayed or not.
     * 3. If not, check if the permission is requested for the first time or not.
     * 4. If yes, save that in preferences and request permission.
     * 5. If not, then the permission is permanently denied.
     */
    private fun initPermissionModel() {
        Timber.d("Initializing permission model")
        if (!hasPermission()) {
            //doesn't have permission, checking if user has been asked for permission earlier
            if (shouldShowRationale()) {
                // should show rationale
                Timber.d("should display rationale")
                showPermissionRationale()
            } else {
                if (isPermissionAskedFirstTime()) {
                    Timber.d("permission asked first time")
                    // request permission
                    setPermissionAsked()
                    requestPermission()
                } else {
                    // permanently denied
                    showPermanentlyDeniedDialog()
                }
            }
        } else {
            // permission is already granted
            onPermissionGranted()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SETTINGS_REQUEST_CODE -> {
                reinitializeTimer()
            }
        }
    }

    private fun reinitializeTimer() {
        if (appPref.getBoolean(SettingsFragment.AUTO_REFRESH, false)) {
            stopAutoRefresh()
            startAutoRefresh()
        } else {
            stopAutoRefresh()
        }
    }

    private fun startAutoRefresh() {
        val interval =
            appPref.getString(SettingsFragment.AUTO_REFRESH_INTERVAL, "10")?.toLongOrNull()
                ?: 10
        refreshScope.launch {
            while (true) {
                delay(TimeUnit.SECONDS.toMillis(interval))
                withContext(Dispatchers.Main) {
                    refreshLiveData.postValue(true)
                }
            }
        }
    }

    private fun stopAutoRefresh() {
        refreshJob.cancel()
        refreshJob = SupervisorJob()
        refreshScope = CoroutineScope(Dispatchers.IO + refreshJob)
        Timber.e("Auto refresh stopped")
    }

    /**
     * handles the flow when the permission is granted successfully. It either sends success broadcast if the permission is granted and location setting resolution is disabled or proceeds for checking location settings
     */
    private fun onPermissionGranted() {
        startScan()
    }

    /**
     * Shows permission permanently blocked dialog
     */
    private fun showPermanentlyDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle(com.proximipro.engage.android.R.string.permission_blocked_title)
            .setMessage(com.proximipro.engage.android.R.string.permission_blocked_desc)
            .setPositiveButton(com.proximipro.engage.android.R.string.open_settings) { dialog, _ ->
                openSettings()
                dialog.dismiss()
            }
            .setNegativeButton(com.proximipro.engage.android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                showPermanentlyDeniedDialog()
            }
            .setCancelable(false)
            .create()
            .takeIf { !isFinishing }?.show()
    }

    /**
     * Opens app settings screen
     */
    private fun openSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, SETTINGS_ACTIVITY_REQUEST_CODE)
    }

    /**
     * Determines whether the rationale needs to be shown or not
     * @return Boolean true if needs to be shown, false otherwise
     */
    private fun shouldShowRationale(): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            COARSE_LOCATION_PERMISSION
        ) || ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            FINE_LOCATION_PERMISSION
        )
    }

    /**
     * Checks whether the app has location permission or not
     * @return true is the app has location permission, false otherwise.
     * */
    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            COARSE_LOCATION_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Displays a permission rationale dialog
     */
    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Location permission required!")
            .setMessage("Location permission is required in order to use this feature properly.Please grant the permission.")
            .setPositiveButton(com.proximipro.engage.android.R.string.grant) { dialog, _ ->
                requestPermission()
                dialog.dismiss()
            }
            .setNegativeButton(com.proximipro.engage.android.R.string.deny) { dialog, _ ->
                dialog.dismiss()
                onPermissionDenied()
            }
            .setCancelable(false)
            .create()
            .takeIf { !isFinishing }?.show()
    }

    /**
     * Sends denied broadcast when user denies to grant location permission
     */
    private fun onPermissionDenied() {
        Timber.d("Sending permission denied")
        showSnackBar(root, "Cannot proceed without location permission")
    }

    /**
     * Checks whether the requested permission is asked for the first time or not.
     *
     * The value is stored in the shared preferences.
     * @receiver Fragment is used to get context.
     * @return Boolean true if the permission is asked for the first time, false otherwise.
     */
    private fun isPermissionAskedFirstTime(): Boolean =
        pref.getBoolean(FINE_LOCATION_PERMISSION, true)

    /**
     * Writes the false value into shared preferences which indicates that the location permission has been requested previously.
     * @receiver Fragment is used to get context.
     */
    private fun setPermissionAsked() =
        pref.edit().putBoolean(FINE_LOCATION_PERMISSION, false).commit()

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushReceiver)
    }

    /**
     * Actual request for the permission
     * */
    private fun requestPermission() =
        ActivityCompat.requestPermissions(
            this,
            arrayOf(FINE_LOCATION_PERMISSION, COARSE_LOCATION_PERMISSION),
            PERMISSION_REQUEST_CODE
        )

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
            engage.config().userConfig.gender
        }
    }

    inner class PushMessageBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Timber.e("Received push in activity")
            val message =
                intent.getParcelableExtra<RemoteMessage>(Constants.INTENT_KEY_PUSH) ?: return
            showPushDialog(message)
        }
    }

    private fun showPushDialog(remoteMessage: RemoteMessage) {
        val map = remoteMessage.data.toMap()
        val message: String = map["message"] ?: return
        AlertDialog.Builder(this)
            .setTitle(map["title"])
            .setMessage(message)
            .setPositiveButton(R.string.accept) { dialog, _ ->
                logPushOpenEvent(remoteMessage.data["notification_id"] ?: "")
                loadPushContent(map["url"].toString())
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .create().takeUnless { isFinishing }?.show()
    }

    private fun logPushOpenEvent(notificationID: String) {
        engage.logPushOpenEvent(notificationID).onFailure(Timber::e)
    }

    /**
     * Fetches push content using given url
     */
    private fun loadPushContent(url: String) {
        engage.getContentLoader().fetchContent(url).onSuccess {
            onNewContentFound(it)
        }.onFailure {
            Timber.e(it)
        }
    }
}