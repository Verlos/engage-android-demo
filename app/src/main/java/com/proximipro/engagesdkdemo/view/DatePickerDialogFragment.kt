package com.proximipro.engagesdkdemo.view

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

/*
 * Created by Birju Vachhani on 24 September 2019
 * Copyright © 2019 engage-sdk. All rights reserved.
 */

class DatePickerDialogFragment(private val onChanged: (view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) -> Unit) :
    DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        onChanged(view, year, month + 1, dayOfMonth)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireActivity(), this, year, month, day).apply {
            datePicker.maxDate = Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time.time
            datePicker.calendarViewShown = false
        }
    }
}