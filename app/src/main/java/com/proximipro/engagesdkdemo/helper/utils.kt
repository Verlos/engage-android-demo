package com.proximipro.engagesdkdemo.helper

import android.view.View
import com.google.android.material.snackbar.Snackbar

/*
 * Created by Birju Vachhani on 30 May 2019
 * Copyright © 2019 ProximiProDemo. All rights reserved.
 */

/**
 * Top level function to display [Snackbar]
 * @param root View
 * @param msg String
 * @return Snackbar
 */
fun showSnackBar(root: View, msg: String) = Snackbar.make(root, msg, Snackbar.LENGTH_LONG)
    .apply {
        setAction("Dismiss") { dismiss() }
        show()
    }