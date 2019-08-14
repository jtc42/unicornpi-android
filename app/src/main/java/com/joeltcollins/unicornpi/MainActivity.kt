/*
 * Copyright (c) 2016. Truiton (http://www.truiton.com/).
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

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout

import java.net.URL
import khttp.get
import khttp.responses.Response

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.json.JSONObject

// TODO: Support multiple devices, with list of hosts in preferences, and spinner in top bar

class MainActivity : AppCompatActivity() {

    // Set up frameLayout object
    private var frameLayout: FrameLayout? = null

    // Generate the API base URL from preferences
    // Handle preferences and API URL
    val apiBase: String
        get() {
            val pref = PreferenceManager.getDefaultSharedPreferences(this)

            val deviceIP = pref.getString("prefs_device_ip", "0.0.0.0")
            val apiVersion = pref.getString("prefs_api_version", "v1")
            return "http://$deviceIP/api/$apiVersion/"
        }


    // ONCREATE FUNCTIONS
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Populate frameLayout object
        frameLayout = findViewById(R.id.frame_layout)

        //Set up bottom navigation bar and populate

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.action_item1 -> selectedFragment = ItemOneFragment.newInstance()
                R.id.action_item2 -> selectedFragment = ItemTwoFragment.newInstance()
                R.id.action_item3 -> selectedFragment = ItemThreeFragment.newInstance()
            }
            addFragment(selectedFragment!!)
            true
        }


        //Manually displaying the first fragment - one time only
        val selectedFragment = ItemOneFragment.newInstance()
        addFragment(selectedFragment)

        //Handle intents, for launcher shortcuts
        val iin = intent
        val bnd = iin.extras

        //If intent extras exist
        if (bnd != null) {
            // Create a string 'intentArgument' based on intent extra 'TEXT'
            val intentArgument = bnd.get("android.intent.extra.TEXT") as String

            var params: Map<String, String> = mapOf()

            if (intentArgument == "status/clear") {
                params = mapOf("global_status" to "0")
            }
            else if (intentArgument == "clamp/evening") {
                params = mapOf(
                        "static_clamp_hex" to "ff880d",
                        "global_mode" to "clamp",
                        "global_status" to "1"
                )
            }
            else if (intentArgument == "brightness/80") {
                params = mapOf(
                        "global_brightness_val" to "80",
                        "global_status" to "1"
                )
            }
            else if (intentArgument == "brightness/20") {
                params = mapOf(
                        "global_brightness_val" to "20",
                        "global_status" to "1"
                )
            }

            retreiveAsync("state", params, false, method = "POST")
        }

    }

    //GUI SETUP FUNCTIONS
    //Expand the main menu into the menu bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    //Map menu items to functions
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.itemId) {
            R.id.menu_main_clear -> {
                val params = mapOf(
                        "global_status" to "0"
                )
                retreiveAsync("state", params, false, method = "POST")
                return true
            }
            R.id.menu_main_settings -> {
                val i = Intent(this, SettingsActivity::class.java)
                startActivity(i)
                return true
            }
            R.id.menu_main_about -> {
                val versionName = BuildConfig.VERSION_NAME
                showSnack("Unicorn Pi for Android, version $versionName")
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }


    // BASIC FUNCTIONALITY
    // add/replace fragment in container [framelayout]
    private fun addFragment(fragment: Fragment) {
        // Hide fragment layout during transaction, while loading spinner activates
        toggleLoader(true)
        // Create fragment
        supportFragmentManager
                .beginTransaction()
                // TODO: Fix references to private animation. Use android.R.anim, or copy anim XML
                .setCustomAnimations(R.anim.abc_fade_in, R.anim.design_bottom_sheet_slide_out)
                .replace(R.id.frame_layout, fragment, fragment.javaClass.simpleName)
                .addToBackStack(fragment.javaClass.simpleName)
                .commit()
    }

    // Toggle loading spinner
    fun toggleLoader(isLoading: Boolean){
        if (isLoading) {
            progress_layout.visibility = View.VISIBLE
            frame_layout.visibility = View.GONE
        }
        else {
            progress_layout.visibility = View.GONE
            frame_layout.visibility = View.VISIBLE
        }
    }

    //Show a snack, with message passed as an argument
    fun showSnack(message: String) =
            Snackbar.make(findViewById<View>(R.id.placeSnackBar), message, Snackbar.LENGTH_LONG)
                    .show()

    // Return string of content returned from a URL
    fun suspendedGetFromURL(url_string: String, params: Map<String, String>, method: String = "GET"): JSONObject? {
        return try {
            val responseObj: Response

            Log.i("INFO", method)
            // Handle non-GET methods
            if (method == "POST") {
                responseObj = khttp.post(url_string, json = params)
            }
            // Default to GET
            else {
                responseObj = khttp.get(url_string, params = params)
            }

            val jsonObj : JSONObject = responseObj.jsonObject

            // Log successful response
            Log.i("INFO", "Response obtained from $url_string")
            Log.i("INFO", responseObj.text)
            // Return response
            jsonObj
        } catch (e: Exception) {
            showSnack("Error connecting to device")
            // Log any errors
            Log.e("ERROR", e.message, e)
            //If connection failed, return null string
            null
        }
    }

    // Get and process HTTP response in a coroutine
    private fun retreiveAsync(
            api_arg: String,
            params: Map<String, String>,
            show_progress: Boolean,
            redraw_all: Boolean = false,
            method: String = "GET"){

        // Launch a new coroutine that executes in the Android UI thread
        GlobalScope.launch(Dispatchers.Main){

            // Start loader
            if (show_progress) {toggleLoader(true)}

            // Suspend while data is obtained
            val response = withContext(Dispatchers.Default) {
                suspendedGetFromURL(apiBase+api_arg, params, method=method)
            }

            // Call function to handle response string, only if response not null
            if (response != null) {
                // Stop loader
                if (show_progress) {toggleLoader(false)}
            }
        }
    }


}
