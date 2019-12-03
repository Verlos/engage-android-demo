package com.proximipro.engagesdkdemo.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.proximipro.engage.android.model.ProBeacon
import com.proximipro.engagesdkdemo.R
import kotlinx.android.synthetic.main.beacon_details_dialog.ivClose
import kotlinx.android.synthetic.main.beacon_details_dialog.tvAddress
import kotlinx.android.synthetic.main.beacon_details_dialog.tvDistance
import kotlinx.android.synthetic.main.beacon_details_dialog.tvEvent
import kotlinx.android.synthetic.main.beacon_details_dialog.tvMajor
import kotlinx.android.synthetic.main.beacon_details_dialog.tvMinor
import kotlinx.android.synthetic.main.beacon_details_dialog.tvRssi
import kotlinx.android.synthetic.main.beacon_details_dialog.tvTxPower
import kotlinx.android.synthetic.main.beacon_details_dialog.tvUuid

/*
 * Created by Birju Vachhani on 10 June 2019
 * Copyright © 2019 engage-sdk. All rights reserved.
 */

/**
 * Dialog controller for displaying beacon related information
 */
class BeaconDetailsDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "BeaconDetailsDialogFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.beacon_details_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.apply {
            val beacon = getParcelable<ProBeacon>("beacon") ?: return
            val event = getString("event") ?: return

            setBeacon(beacon, event)
        }

        ivClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setBeacon(beacon: ProBeacon, event: String) {
        tvAddress.text = beacon.bluetoothAddress
        tvMajor.text = beacon.major.toString()
        tvMinor.text = beacon.minor.toString()
        tvRssi.text = beacon.rssi.toString()
        tvTxPower.text = beacon.txPower.toString()
        tvDistance.text = getString(R.string.distance_format, beacon.distance)
        tvUuid.text = beacon.uuid
        tvEvent.text = event.capitalize()
    }

}