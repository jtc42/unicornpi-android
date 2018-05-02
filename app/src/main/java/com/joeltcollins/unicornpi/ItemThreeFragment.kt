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

import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_item_three.*
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener


class ItemThreeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //Get current view
        val v = inflater.inflate(R.layout.fragment_item_three, container, false)

        //RAINBOW START BUTTON LISTENER & FUNCTIONS
        rainbow_start_button.setOnClickListener {
            val spinner_position = rainbow_theme_spinner.selectedItemPosition
            val speed_pc = rainbow_speed_seekbar.progress
            val speed = (speed_pc + 1) / 100.toFloat()

            RetrieveFeedTask(v, "rainbow/set?mode=${spinner_position.toString()}&speed=${speed.toString()}&status=1", true).execute()
        }

        //ALSA START BUTTON LISTENER & FUNCTIONS
        alsa_start_button.setOnClickListener {
            val spinner_position = alsa_theme_spinner.selectedItemPosition
            val sensitivity = alsa_sensitivity_seekbar.progress / 10
            val mic = alsa_mic_seekbar.progress
            val vol = alsa_vol_seekbar.progress

            RetrieveFeedTask(v, "alsa/set?mode=${spinner_position.toString()}&sensitivity=${sensitivity.toString()}&monitor=${mic.toString()}&volume=${vol.toString()}&status=1", true).execute()
        }


        //GET API RESPONSE FOR UI STARTUP
        RetrieveFeedTask(v, "status/all", true).execute()

        return v
    }

    //RETREIVEFEED CLASS
    internal inner class RetrieveFeedTask(rootView: View,
                                          private val api_arg: String,
                                          private val show_progress: Boolean) : AsyncTask<Void, Void, String>() {

        //Get mainactivity for sending snackbars etc
        val activity: MainActivity = getActivity() as MainActivity

        //Before executing asynctask
        override fun onPreExecute() {
            //Show progressbars, hide content
            if (show_progress) {
                anim_progressLayout.visibility = View.VISIBLE
                anim_mainLayout.visibility = View.GONE
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
                anim_progressLayout.visibility = View.GONE
                anim_mainLayout.visibility = View.VISIBLE
            }

        }
    }

    //HANDLE JSON RESPONSE
    private fun handleResponse(response: String) {
        try {
            val `object` = JSONTokener(response).nextValue() as JSONObject
            if (`object`.has("dynamic_rainbow_mode")) {
                val response_rainbow_mode = `object`.getInt("dynamic_rainbow_mode")
                rainbow_theme_spinner.setSelection(response_rainbow_mode)
            }
            if (`object`.has("dynamic_rainbow_speed")) {
                val response_rainbow_speed = `object`.getString("dynamic_rainbow_speed")
                val speed_pc = Math.round(java.lang.Float.parseFloat(response_rainbow_speed) * 100) - 1
                rainbow_speed_seekbar.progress = speed_pc
            }
            if (`object`.has("dynamic_alsa_enabled")) {
                val response_alsa_enabled = `object`.getInt("dynamic_alsa_enabled")
                if (response_alsa_enabled == 0) {
                    card_anim_alsa.visibility = View.GONE
                } else {
                    card_anim_alsa.visibility = View.VISIBLE
                }
            }
            if (`object`.has("dynamic_alsa_mode")) {
                val response_alsa_mode = `object`.getInt("dynamic_alsa_mode")
                alsa_theme_spinner.setSelection(response_alsa_mode)
            }
            if (`object`.has("dynamic_alsa_sensitivity")) {
                val response_alsa_sensitivity = `object`.getInt("dynamic_alsa_sensitivity")
                alsa_sensitivity_seekbar.progress = response_alsa_sensitivity * 10
            }
            if (`object`.has("dynamic_alsa_monitor")) {
                val response_alsa_mic = `object`.getInt("dynamic_alsa_monitor")
                alsa_mic_seekbar.progress = response_alsa_mic
            }
            if (`object`.has("dynamic_alsa_volume")) {
                val response_alsa_vol = `object`.getInt("dynamic_alsa_volume")
                alsa_vol_seekbar.progress = response_alsa_vol
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    companion object {
        fun newInstance(): ItemThreeFragment {
            return ItemThreeFragment()
        }
    }

}
