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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

import kotlinx.android.synthetic.main.fragment_item_two.*


class ItemTwoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //Get current view
        return inflater.inflate(R.layout.fragment_item_two, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // COLOR PICKER LISTENER
        color_picker_view.addOnColorSelectedListener { selectedColor ->
            //RGB hex string of selected color
            val hexString = Integer.toHexString(selectedColor).toUpperCase().substring(2, 8)

            val params = mapOf(
                    "static_clamp_hex" to hexString,
                    "global_mode" to "clamp",
                    "global_status" to "1"
            )
            retreiveAsync("state", params, false, method = "POST")
        }

        // CLAMP BUTTON LISTENER & FUNCTIONS
        clamp_button_night.setOnClickListener {
            val params = mapOf(
                    "static_clamp_hex" to "ff880d",
                    "global_mode" to "clamp",
                    "global_status" to "1"
            )
            retreiveAsync("state", params, false, method = "POST")
        }

        // CLAMP BUTTON LISTENER & FUNCTIONS
        clamp_button_evening.setOnClickListener {
            val params = mapOf(
                    "static_clamp_hex" to "ff9f46",
                    "global_mode" to "clamp",
                    "global_status" to "1"
            )
            retreiveAsync("state", params, false, method = "POST")
        }

        // CLAMP BUTTON LISTENER & FUNCTIONS
        clamp_button_desk.setOnClickListener {
            val params = mapOf(
                    "static_clamp_hex" to "ffc08c",
                    "global_mode" to "clamp",
                    "global_status" to "1"
            )
            retreiveAsync("state", params, false, method = "POST")
        }

        // CLAMP BUTTON LISTENER & FUNCTIONS
        clamp_button_day.setOnClickListener {
            val params = mapOf(
                    "static_clamp_hex" to "ffe4cd",
                    "global_mode" to "clamp",
                    "global_status" to "1"
            )
            retreiveAsync("state", params, false, method = "POST")
        }

        // GET API RESPONSE FOR UI STARTUP
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

            //If global status is returned, route music have been status/all, so brightness slider should be updated
            if (responseObject.has("static_clamp_hex")) {
                val responseClampHex = responseObject.getString("static_clamp_hex")

                val colorInt = java.lang.Long.parseLong("ff$responseClampHex", 16).toInt()
                color_picker_view!!.setColor(colorInt, false)

            }
        } catch (e: JSONException) {e.printStackTrace()}
    }

    companion object {
        fun newInstance(): ItemTwoFragment {
            return ItemTwoFragment()
        }
    }
}
