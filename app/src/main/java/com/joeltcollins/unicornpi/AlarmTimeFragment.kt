package com.joeltcollins.unicornpi

import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.TextView
import android.app.DialogFragment
import android.app.Dialog
import android.widget.TimePicker


class AlarmTimeFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {

        //Set up the textview object to grab the currently set time
        val alarm_time_text = activity.findViewById<TextView>(R.id.alarm_time_text)

        //Get current time value from the textview
        val current_time = alarm_time_text.text.toString()
        val parts = current_time.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        val current_hours = Integer.parseInt(parts[0])
        val current_mins = Integer.parseInt(parts[1])

        //Create and return a new instance of TimePickerDialog
        return TimePickerDialog(activity, this, current_hours, current_mins, DateFormat.is24HourFormat(activity))
    }

    //onTimeSet() callback method
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        //Do something with the user chosen time
        //Get reference of host activity (XML Layout File) TextView widget
        val alarm_time_text = activity.findViewById<TextView>(R.id.alarm_time_text)

        val formatted_time = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute)

        //Set time text
        alarm_time_text.setText(formatted_time)
    }
}