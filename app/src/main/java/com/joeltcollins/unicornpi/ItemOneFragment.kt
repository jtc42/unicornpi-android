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
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import android.os.AsyncTask

import kotlinx.android.synthetic.main.fragment_item_one.*


class ItemOneFragment : Fragment() {

    //Grab resources
    private var alarm_status_active: String = getString(R.string.alarm_status_active)
    private var alarm_status_inactive: String =getString(R.string.alarm_status_inactive)
    private var fade_status_active: String = getString(R.string.fade_status_active)
    private var fade_status_inactive: String = getString(R.string.fade_status_inactive)
    private var status_active: Int = ContextCompat.getColor(context!!, R.color.label_active)
    private var status_inactive: Int = ContextCompat.getColor(context!!, R.color.label_inactive)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //Get current view
        val v = inflater.inflate(R.layout.fragment_item_one, container, false)

        //BRIGHTNESS SEEK LISTENER & FUNCTIONS
        brightness_seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            internal var progress = 0

            override fun onProgressChanged(seekBar: SeekBar, progresValue: Int, fromUser: Boolean) {
                progress = progresValue
                brightness_text.text = Integer.toString(progress)
                if (fromUser) { //Blocks API call if UI is just updating (caused fades to stop on app load)
                    RetrieveFeedTask(v, "brightness/set?val=${progress.toString()}", false).execute()
                }
                if (fade_status.text.toString() == fade_status_active) {
                    fade_status.text = fade_status_inactive
                    fade_status.setTextColor(status_inactive)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //activity.showSnack("Started seek");
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                RetrieveFeedTask(v, "brightness/set?val=${progress.toString()}", false).execute()
                //activity.showSnack("Brightness set");
            }
        })

        //ALARM START BUTTON LISTENER & FUNCTIONS
        fade_start_button.setOnClickListener(object : View.OnClickListener {

            //Get main activity
            internal val activity: MainActivity = getActivity() as MainActivity

            override fun onClick(view: View) {
                activity.showSnack("Fade started")
                val minutes = Integer.parseInt(fade_time!!.text.toString())
                val target = fade_target_seekbar!!.progress / 100.toFloat()

                RetrieveFeedTask(v, "fade/set?minutes=${minutes.toString()}&target=${target.toString()}&status=1", false).execute()
            }
        })

        //ALARM START BUTTON LISTENER & FUNCTIONS
        fade_stop_button.setOnClickListener(object : View.OnClickListener {

            //Get main activity
            internal val activity: MainActivity = getActivity() as MainActivity

            override fun onClick(view: View) {
                activity.showSnack("Fade stopped")
                RetrieveFeedTask(v, "fade/set?&status=0", false).execute()
            }
        })

        //ALARM TIME BUTTON LISTENER & FUNCTIONS
        alarm_time_button.setOnClickListener(object : View.OnClickListener {

            //Get main activity
            internal val activity: MainActivity = getActivity() as MainActivity

            override fun onClick(view: View) {
                //Start up timepicker fragment
                //NOTE: ONTIMECHANGE IS HANDLES BY THE ALARMTIMEFRAGMENT
                //DRAWING TO UI SHOULD BE HANDLED THERE
                val newFragment = AlarmTimeFragment()
                newFragment.show(activity.fragmentManager, "TimePicker")
            }
        })

        //ALARM START BUTTON LISTENER & FUNCTIONS
        alarm_start_button.setOnClickListener(object : View.OnClickListener {

            //Get main activity
            internal val activity: MainActivity = getActivity() as MainActivity

            override fun onClick(view: View) {
                activity.showSnack("Alarm set")
                val lead = Integer.parseInt(alarm_lead.text.toString())
                val tail = Integer.parseInt(alarm_tail.text.toString())
                val time = alarm_time_text.text.toString()

                RetrieveFeedTask(v, "alarm/set?lead=${lead.toString()}&tail=${tail.toString()}&time=$time&status=1", false).execute()
            }
        })

        //ALARM START BUTTON LISTENER & FUNCTIONS
        alarm_stop_button.setOnClickListener(object : View.OnClickListener {

            //Get main activity
            internal val activity: MainActivity = getActivity() as MainActivity

            override fun onClick(view: View) {
                activity.showSnack("Alarm unset")
                RetrieveFeedTask(v, "alarm/set?&status=0", false).execute()
            }
        })

        //GET API RESPONSE FOR UI STARTUP
        RetrieveFeedTask(v, "status/all", true).execute()

        return v
    }

    //RETREIVEFEED CLASS
    // TODO: Better, more Kotlin-y async task in all fragments
    internal inner class RetrieveFeedTask(rootView: View,
                                          private val api_arg: String,
                                          private val show_progress: Boolean) : AsyncTask<Void, Void, String>() {

        //Get mainactivity for sending snackbars etc
        val activity: MainActivity = getActivity() as MainActivity

        //Before executing asynctask
        override fun onPreExecute() {
            //Show progressbars, hide content
            if (show_progress) {
                system_progressLayout.visibility = View.VISIBLE
                system_mainLayout.visibility = View.GONE
            }
        }

        //Main asynctask
        override fun doInBackground(vararg params: Void): String? {
            return activity.getFromURL(activity.apiBase + api_arg)
        }

        //After executing asynctask
        override fun onPostExecute(response: String?) {
            if (response == null) {
                activity.showSnack("Error when parsing response")
            } else {
                //Call function to handle response string, only if response not null
                handleResponse(response)
                //Log response to debug terminal
                Log.i("INFO", response)
            }

            //Hide progressbar, show content
            if (show_progress) {
                system_progressLayout.visibility = View.GONE
                system_mainLayout.visibility = View.VISIBLE
            }

        }
    }

    //HANDLE JSON RESPONSE
    private fun handleResponse(response: String) {
        var update_brightness_slider = false
        try {
            // TODO: Rename this weird `object` object in all fragments
            val `object` = JSONTokener(response).nextValue() as JSONObject
            //If global status is returned, route music have been status/all, so brightness slider should be updated
            if (`object`.has("global_status")) {
                update_brightness_slider = true
            }
            if (`object`.has("global_brightness_val")) {
                val response_brightness_val = `object`.getInt("global_brightness_val")
                if (update_brightness_slider) {
                    brightness_seekbar!!.progress = response_brightness_val
                }
            }
            if (`object`.has("special_alarm_time")) {
                val alarm_time = `object`.getString("special_alarm_time")

                val parts = alarm_time.split(":".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                val current_hours = Integer.parseInt(parts[0])
                val current_mins = Integer.parseInt(parts[1])

                val formatted_time = String.format("%02d", current_hours) + ":" + String.format("%02d", current_mins)

                alarm_time_text.setText(formatted_time)
            }
            if (`object`.has("special_alarm_lead")) {
                val response_alarm_lead = `object`.getInt("special_alarm_lead")
                alarm_lead!!.setText(response_alarm_lead.toString())
            }
            if (`object`.has("special_alarm_tail")) {
                val response_alarm_tail = `object`.getInt("special_alarm_tail")
                alarm_tail!!.setText(response_alarm_tail.toString())
            }
            if (`object`.has("special_alarm_status")) {
                val response_alarm_status = `object`.getInt("special_alarm_status")
                if (response_alarm_status == 1) {
                    alarm_status.text = alarm_status_active
                    alarm_status.setTextColor(status_active)
                } else {
                    alarm_status.text = alarm_status_inactive
                    alarm_status.setTextColor(status_inactive)
                }
            }
            if (`object`.has("special_fade_minutes")) {
                val response_fade_minutes = `object`.getInt("special_fade_minutes")
                fade_time!!.setText(response_fade_minutes.toString())
            }
            if (`object`.has("special_fade_target")) {
                val response_fade_target = `object`.getString("special_fade_target")
                val fade_percent = Math.round(java.lang.Float.parseFloat(response_fade_target) * 100)
                fade_target_seekbar!!.progress = fade_percent
            }
            if (`object`.has("special_fade_status")) {
                val response_alarm_status = `object`.getInt("special_fade_status")
                if (response_alarm_status == 1) {
                    fade_status.text = fade_status_active
                    fade_status.setTextColor(status_active)
                } else {
                    fade_status.text = fade_status_inactive
                    fade_status.setTextColor(status_inactive)
                }
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    companion object {
        fun newInstance(): ItemOneFragment {
            return ItemOneFragment()
        }
    }

}
