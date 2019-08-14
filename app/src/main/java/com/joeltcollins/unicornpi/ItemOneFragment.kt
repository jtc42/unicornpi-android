/*
 * Copyright (c) 2017. Truiton (http://www.truiton.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Mohit Gupt (https://github.com/mohitgupt)
 *
 */

package com.joeltcollins.unicornpi

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

import kotlinx.coroutines.*

import kotlinx.android.synthetic.main.fragment_item_one.*


class ItemOneFragment : Fragment() {


    // While creating view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get current view
        return inflater.inflate(R.layout.fragment_item_one, container, false)
    }


    // Once view has been inflated/created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // Grab resources from XML
        val fadeStatusActive: String = getString(R.string.fade_status_active)
        val fadeStatusInactive: String = getString(R.string.fade_status_inactive)
        val statusInactive: Int = ContextCompat.getColor(context!!, R.color.label_inactive)

        // BRIGHTNESS SEEK LISTENER & FUNCTIONS
        brightness_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var progress = 0

            override fun onProgressChanged(seekBar: SeekBar, progresValue: Int, fromUser: Boolean) {
                progress = progresValue
                brightness_text.text = "$progress"
                if (fromUser) { // Blocks API call if UI is just updating (caused fades to stop on app load)
                    // OLD
                    // val params = mapOf("val" to progress.toString())
                    // retreiveAsync("brightness/set", params, false)

                    //NEW
                    val params = mapOf("global_brightness_val" to progress.toString())
                    retreiveAsync("state", params, false, method="POST")
                }
                if (fade_status.text.toString() == fadeStatusActive) {
                    fade_status.text = fadeStatusInactive
                    fade_status.setTextColor(statusInactive)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                val params = mapOf("global_brightness_val" to progress.toString())
                retreiveAsync("state", params, false, method="POST")
            }
        })

        // FADE START BUTTON LISTENER & FUNCTIONS
        fade_start_button.setOnClickListener(object : View.OnClickListener {

            //Get main activity
            val activity: MainActivity = getActivity() as MainActivity

            override fun onClick(view: View) {
                activity.showSnack("Fade started")
                val minutes = Integer.parseInt(fade_time!!.text.toString())
                val target = fade_target_seekbar!!.progress / 100.toFloat()

                val params = mapOf(
                        "special_fade_minutes" to minutes.toString(),
                        "special_fade_target" to target.toString(),
                        "special_fade_status" to "1"
                )
                retreiveAsync("state", params, false, method = "POST")
            }
        })

        // FADE START BUTTON LISTENER & FUNCTIONS
        fade_stop_button.setOnClickListener(object : View.OnClickListener {

            //Get main activity
            val activity: MainActivity = getActivity() as MainActivity

            override fun onClick(view: View) {
                activity.showSnack("Fade stopped")

                val params = mapOf(
                        "special_fade_status" to "0"
                )
                retreiveAsync("state", params, false, method = "POST")
            }
        })

        // ALARM TIME BUTTON LISTENER & FUNCTIONS
        alarm_time_button.setOnClickListener(object : View.OnClickListener {

            //Get main activity
            val activity: MainActivity = getActivity() as MainActivity

            override fun onClick(view: View) {
                // Start timepicker fragment
                // onTimeSet, and UI updating, is handled by AlarmTimeFragment
                val newFragment = AlarmTimeFragment()
                newFragment.show(activity.fragmentManager, "TimePicker")
            }
        })

        // ALARM START BUTTON LISTENER & FUNCTIONS
        alarm_start_button.setOnClickListener(object : View.OnClickListener {

            //Get main activity
            val activity: MainActivity = getActivity() as MainActivity

            override fun onClick(view: View) {
                activity.showSnack("Alarm set")
                val lead = alarm_lead.text.toString()
                val tail = alarm_tail.text.toString()
                val time = alarm_time_text.text.toString()

                val params = mapOf(
                        "special_alarm_lead" to lead,
                        "special_alarm_tail" to tail,
                        "special_alarm_time" to time,
                        "special_alarm_status" to "1"
                )

                retreiveAsync("state", params, false, method = "POST")
            }
        })

        // ALARM START BUTTON LISTENER & FUNCTIONS
        alarm_stop_button.setOnClickListener(object : View.OnClickListener {

            //Get main activity
            val activity: MainActivity = getActivity() as MainActivity

            override fun onClick(view: View) {
                activity.showSnack("Alarm unset")

                val params = mapOf("special_alarm_status" to "0")
                retreiveAsync("state", params, false, method = "POST")
            }
        })

        // GET API RESPONSE FOR UI STARTUP
        // Happens here because post-execute uses UI elements
        retreiveAsync("state", mapOf(), true, redraw_all = true, method = "GET")

    }


    // Get and process HTTP response in a coroutine
    private fun retreiveAsync(
            api_arg: String,
            params: Map<String, String>,
            show_progress: Boolean,
            redraw_all: Boolean = false,
            method: String = "GET"){
        val activity: MainActivity = activity as MainActivity

        // Launch a new coroutine that executes in the Android UI thread
        GlobalScope.launch(Dispatchers.Main){

            // Start loader
            if (show_progress) {activity.toggleLoader(true)}

            // Suspend while data is obtained
            val response = withContext(Dispatchers.Default) {
                activity.suspendedGetFromURL(activity.apiBase+api_arg, params, method=method)
            }

            // Call function to handle response string, only if response not null
            if (response != null) {
                // If fragment layout is not null (ie. fragment still in view), handle response
                if (frag_layout != null) {handleResponse(response, redraw_all=redraw_all)}
                // Else log that handling has been aborted
                else {Log.i("INFO", "Response processing aborted")}
                // Stop loader
                if (show_progress) {activity.toggleLoader(false)}
            }
        }
    }


    //HANDLE JSON RESPONSE
    private fun handleResponse(responseObject: JSONObject, redraw_all: Boolean = false) {
        var updateBrightnessSlider = false

        //Grab resources from XML
        val alarmStatusActive: String = getString(R.string.alarm_status_active)
        val alarmStatusInactive: String = getString(R.string.alarm_status_inactive)
        val fadeStatusActive: String = getString(R.string.fade_status_active)
        val fadeStatusInactive: String = getString(R.string.fade_status_inactive)
        val statusActive: Int = ContextCompat.getColor(context!!, R.color.label_active)
        val statusInactive: Int = ContextCompat.getColor(context!!, R.color.label_inactive)

        try {

            // If global status is returned, route music have been status/all, so brightness slider should be updated
            if (redraw_all) {
                updateBrightnessSlider = true
            }

            if (responseObject.has("global_brightness_val")) {
                val responseBrightnessVal = responseObject.getInt("global_brightness_val")
                if (updateBrightnessSlider) {
                    brightness_seekbar.progress = responseBrightnessVal
                }
            }

            if (responseObject.has("special_alarm_time")) {
                val alarmTime = responseObject.getString("special_alarm_time")

                val parts = alarmTime.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                val currentHours = Integer.parseInt(parts[0])
                val currentMins = Integer.parseInt(parts[1])

                val formattedTime = String.format("%02d", currentHours) + ":" + String.format("%02d", currentMins)

                alarm_time_text.text = formattedTime
            }
            if (responseObject.has("special_alarm_lead")) {
                val responseAlarmLead = responseObject.getInt("special_alarm_lead")
                alarm_lead!!.setText(responseAlarmLead.toString())
            }
            if (responseObject.has("special_alarm_tail")) {
                val responseAlarmTail = responseObject.getInt("special_alarm_tail")
                alarm_tail!!.setText(responseAlarmTail.toString())
            }
            if (responseObject.has("special_alarm_status")) {
                val responseAlarmStatus = responseObject.getBoolean("special_alarm_status")
                if (responseAlarmStatus) {
                    alarm_status.text = alarmStatusActive
                    alarm_status.setTextColor(statusActive)
                } else {
                    alarm_status.text = alarmStatusInactive
                    alarm_status.setTextColor(statusInactive)
                }
            }
            if (responseObject.has("special_fade_minutes")) {
                val responseFadeMinutes = responseObject.getInt("special_fade_minutes")
                fade_time!!.setText(responseFadeMinutes.toString())
            }
            if (responseObject.has("special_fade_target")) {
                val responseFadeTarget = responseObject.getString("special_fade_target")
                val fadePercent = Math.round(java.lang.Float.parseFloat(responseFadeTarget) * 100)
                fade_target_seekbar!!.progress = fadePercent
            }
            if (responseObject.has("special_fade_status")) {
                val responseAlarmStatus = responseObject.getBoolean("special_fade_status")
                if (responseAlarmStatus) {
                    fade_status.text = fadeStatusActive
                    fade_status.setTextColor(statusActive)
                } else {
                    fade_status.text = fadeStatusInactive
                    fade_status.setTextColor(statusInactive)
                }
            }

        } catch (e: JSONException) {e.printStackTrace()}
    }

    companion object {
        fun newInstance(): ItemOneFragment {
            val fragmentHome = ItemOneFragment()
            val args = Bundle()
            fragmentHome.arguments = args
            return fragmentHome
        }
    }


}
