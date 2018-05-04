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
        return inflater.inflate(R.layout.fragment_item_three, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //RAINBOW START BUTTON LISTENER & FUNCTIONS
        rainbow_start_button.setOnClickListener {
            val spinnerPosition = rainbow_theme_spinner.selectedItemPosition
            val speedPercent = rainbow_speed_seekbar.progress
            val speed = (speedPercent + 1) / 100.toFloat()

            RetrieveFeedTask("rainbow/set?mode=$spinnerPosition&speed=$speed&status=1", true).execute()
        }

        //ALSA START BUTTON LISTENER & FUNCTIONS
        alsa_start_button.setOnClickListener {
            val spinner_position = alsa_theme_spinner.selectedItemPosition
            val sensitivity = alsa_sensitivity_seekbar.progress / 10
            val mic = alsa_mic_seekbar.progress
            val vol = alsa_vol_seekbar.progress

            RetrieveFeedTask("alsa/set?mode=$spinner_position&sensitivity=$sensitivity&monitor=$mic&volume=$vol&status=1", true).execute()
        }

        //GET API RESPONSE FOR UI STARTUP
        RetrieveFeedTask("status/all", true).execute()

    }


    //RETREIVEFEED CLASS
    internal inner class RetrieveFeedTask(private val api_arg: String,
                                          private val show_progress: Boolean) : AsyncTask<Void, Void, String>() {

        //Get mainactivity for sending snackbars etc
        private val activity: MainActivity = getActivity() as MainActivity

        //Before executing asynctask
        override fun onPreExecute() {
            //Show progressbars, hide content
            if (show_progress) {
                activity.toggleLoader(true)
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
                activity.toggleLoader(false)
            }

        }
    }

    //HANDLE JSON RESPONSE
    private fun handleResponse(response: String) {
        try {
            val responseObject = JSONTokener(response).nextValue() as JSONObject
            if (responseObject.has("dynamic_rainbow_mode")) {
                val responseRainbowMode = responseObject.getInt("dynamic_rainbow_mode")
                rainbow_theme_spinner.setSelection(responseRainbowMode)
            }
            if (responseObject.has("dynamic_rainbow_speed")) {
                val responseRainbowSpeed = responseObject.getString("dynamic_rainbow_speed")
                val speedPercent = Math.round(java.lang.Float.parseFloat(responseRainbowSpeed) * 100) - 1
                rainbow_speed_seekbar.progress = speedPercent
            }
            if (responseObject.has("dynamic_alsa_enabled")) {
                val responseAlsaEnabled = responseObject.getInt("dynamic_alsa_enabled")
                if (responseAlsaEnabled == 0) {
                    card_anim_alsa.visibility = View.GONE
                } else {
                    card_anim_alsa.visibility = View.VISIBLE
                }
            }
            if (responseObject.has("dynamic_alsa_mode")) {
                val responseAlsaMode = responseObject.getInt("dynamic_alsa_mode")
                alsa_theme_spinner.setSelection(responseAlsaMode)
            }
            if (responseObject.has("dynamic_alsa_sensitivity")) {
                val responseAlsaSensitivity = responseObject.getInt("dynamic_alsa_sensitivity")
                alsa_sensitivity_seekbar.progress = responseAlsaSensitivity * 10
            }
            if (responseObject.has("dynamic_alsa_monitor")) {
                val responseAlsaMic = responseObject.getInt("dynamic_alsa_monitor")
                alsa_mic_seekbar.progress = responseAlsaMic
            }
            if (responseObject.has("dynamic_alsa_volume")) {
                val responseAlsaVol = responseObject.getInt("dynamic_alsa_volume")
                alsa_vol_seekbar.progress = responseAlsaVol
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
