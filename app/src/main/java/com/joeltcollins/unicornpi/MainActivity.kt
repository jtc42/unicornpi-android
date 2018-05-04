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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

import java.net.URL

// TODO: Move generic async task to MainActivity. MainActivity to have loading widget, rather than separate for each fragment

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
            val apiVersion = pref.getString("prefs_api_version", "1.0")
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
            // Run async call to intentArgument URL
            retreiveAsync(intentArgument, true)
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
                retreiveAsync("status/clear", false)
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
        supportFragmentManager
                .beginTransaction()
                // .setCustomAnimations(R.anim.design_bottom_sheet_slide_in, R.anim.design_bottom_sheet_slide_out)
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
    suspend fun suspendedGetFromURL(url_string: String): String? {
        return try {
            val response: String = URL(url_string).readText()
            //Log.i("INFO", response)
            response
        } catch (e: Exception) {
            // TODO: Actually handle error properly
            showSnack("Error when parsing response")
            // Log any errors
            Log.e("ERROR", e.message, e)
            //If connection failed, return null string
            null
        }
    }


    // Get and process HTTP response in a coroutine
    private fun retreiveAsync(api_arg: String, show_progress: Boolean){

        // Start loader
        if (show_progress) {
            toggleLoader(true)
        }

        launch{

            // Suspend while data is obtained
            val response: String? = suspendedGetFromURL(apiBase + api_arg)

            launch(UI) { // launch coroutine in UI context
                // Handle response
                if (response == null) {
                    showSnack("Error when parsing response")
                } else {
                    //Call function to handle response string, only if response not null
                    //handleResponse(response)
                    //Log response to debug terminal
                    Log.i("INFO", response)
                }

                // Stop loader
                if (show_progress) {
                    toggleLoader(false)
                }
            }
        }
    }


}
