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
import android.os.AsyncTask
import kotlinx.android.synthetic.main.fragment_item_two.*


class ItemTwoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //Get current view
        val v = inflater.inflate(R.layout.fragment_item_two, container, false)


        //COLOR PICKER LISTENER
        color_picker_view.addOnColorSelectedListener { selectedColor ->
            //RGB hex string of selected color
            val hex_string = Integer.toHexString(selectedColor).toUpperCase().substring(2, 8)
            RetrieveFeedTask(v, "clamp/set?hex=$hex_string&status=1", false).execute()
        }

        //ALARM START BUTTON LISTENER & FUNCTIONS
        clamp_button_night.setOnClickListener {
            RetrieveFeedTask(v, "clamp/set?hex=ff880d&status=1", false).execute()
            //activity.showSnack("Night lamp started");
        }

        //ALARM START BUTTON LISTENER & FUNCTIONS
        clamp_button_evening.setOnClickListener {
            RetrieveFeedTask(v, "clamp/set?hex=ff9f46&status=1", false).execute()
            //activity.showSnack("Evening lamp started");
        }

        //ALARM START BUTTON LISTENER & FUNCTIONS
        clamp_button_desk.setOnClickListener {
            RetrieveFeedTask(v, "clamp/set?hex=ffc08c&status=1", false).execute()
            //activity.showSnack("Desk lamp started");
        }

        //ALARM START BUTTON LISTENER & FUNCTIONS
        clamp_button_day.setOnClickListener {
            RetrieveFeedTask(v, "clamp/set?hex=ffe4cd&status=1", false).execute()
            //activity.showSnack("Day lamp started");
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
                clamp_progressLayout.visibility = View.VISIBLE
                clamp_mainLayout.visibility = View.GONE
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
                clamp_progressLayout.visibility = View.GONE
                clamp_mainLayout.visibility = View.VISIBLE
            }

        }
    }

    //HANDLE JSON RESPONSE
    private fun handleResponse(response: String) {
        try {
            val `object` = JSONTokener(response).nextValue() as JSONObject
            //If global status is returned, route music have been status/all, so brightness slider should be updated
            if (`object`.has("static_clamp_hex")) {
                val response_clamp_hex = `object`.getString("static_clamp_hex")

                val color_int = java.lang.Long.parseLong("ff" + response_clamp_hex, 16).toInt()
                color_picker_view!!.setColor(color_int, false)

            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    companion object {
        fun newInstance(): ItemTwoFragment {
            return ItemTwoFragment()
        }
    }
}
