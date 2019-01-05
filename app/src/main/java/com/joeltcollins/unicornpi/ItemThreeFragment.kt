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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

import kotlinx.android.synthetic.main.fragment_item_three.*


class ItemThreeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Get current view
        return inflater.inflate(R.layout.fragment_item_three, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // RAINBOW START BUTTON LISTENER & FUNCTIONS
        rainbow_start_button.setOnClickListener {
            val spinnerPosition = rainbow_theme_spinner.selectedItemPosition
            val speedPercent = rainbow_speed_seekbar.progress
            val speed = (speedPercent + 1) / 100.toFloat()

            val params = mapOf(
                    "mode" to spinnerPosition.toString(),
                    "speed" to speed.toString(),
                    "status" to "1"
            )

            retreiveAsync("rainbow/set", params,true)
        }

        // ALSA START BUTTON LISTENER & FUNCTIONS
        alsa_start_button.setOnClickListener {
            val spinnerPosition = alsa_theme_spinner.selectedItemPosition
            val sensitivity = alsa_sensitivity_seekbar.progress / 10
            val mic = alsa_mic_seekbar.progress
            val vol = alsa_vol_seekbar.progress

            val params = mapOf(
                    "mode" to spinnerPosition.toString(),
                    "sensitivity" to sensitivity.toString(),
                    "momnitor" to mic.toString(),
                    "volume" to vol.toString(),
                    "status" to "1"
            )

            retreiveAsync("alsa/set", params, true)
        }

        // GET API RESPONSE FOR UI STARTUP
        retreiveAsync("status/all", mapOf(), true)

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
        launch(UI){

            // Start loader
            if (show_progress) {activity.toggleLoader(true)}

            // Suspend while data is obtained
            val response = async(CommonPool) {
                activity.suspendedGetFromURL(activity.apiBase+api_arg, params, method=method)
            }.await()

            // Call function to handle response string, only if response not null
            if (response != null) {
                // If fragment layout is not null (ie. fragment still in view), handle response
                if (frag_layout != null) {handleResponse(response)}
                // Else log that handling has been aborted
                else {Log.i("INFO", "Response processing aborted")}
                // Stop loader
                if (show_progress) {activity.toggleLoader(false)}
            }
        }
    }

    //HANDLE JSON RESPONSE
    private fun handleResponse(responseObject: JSONObject) {
        try {

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
                val responseAlsaEnabled = responseObject.getBoolean("dynamic_alsa_enabled")
                if (!responseAlsaEnabled) {
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

        } catch (e: JSONException) {e.printStackTrace()}
    }

    companion object {
        fun newInstance(): ItemThreeFragment {
            return ItemThreeFragment()
        }
    }

}
