package com.joeltcollins.unicornpi

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TextView
import android.app.DialogFragment
import android.app.Dialog
import android.widget.TimePicker


class AlarmTimeFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        //Set up the textview object to grab the currently set time
        val alarmTimeText = activity.findViewById<TextView>(R.id.alarm_time_text)

        //Get current time value from the textview
        val currentTime = alarmTimeText.text.toString()
        val parts = currentTime.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val currentHours = Integer.parseInt(parts[0])
        val currentMins = Integer.parseInt(parts[1])

        //Create and return a new instance of TimePickerDialog
        return TimePickerDialog(activity, this, currentHours, currentMins, DateFormat.is24HourFormat(activity))
    }

    //onTimeSet() callback method
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        //Do something with the user chosen time
        //Get reference of host activity (XML Layout File) TextView widget
        val alarmTimeText = activity.findViewById<TextView>(R.id.alarm_time_text)

        val formattedTime = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute)

        //Set time text
        alarmTimeText.text = formattedTime
    }
}