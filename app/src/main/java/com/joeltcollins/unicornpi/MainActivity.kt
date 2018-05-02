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
import android.os.AsyncTask
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

import java.net.URL

class MainActivity : AppCompatActivity() {

    // Generate the API base URL from preferences
    // Handle preferences and API URL
    val apiBase: String
        get() {
            val pref = PreferenceManager.getDefaultSharedPreferences(this)

            val deviceIP = pref.getString("prefs_device_ip", "0.0.0.0")
            val devicePort = pref.getString("prefs_device_port", "5000")
            val apiVersion = pref.getString("prefs_api_version", "1.0")
            return "http://$deviceIP:$devicePort/api/$apiVersion/"
        }


    //ONCREATE FUNCTIONS
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Set up bottom navigation bar and populate
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.action_item1 -> selectedFragment = ItemOneFragment.newInstance()
                R.id.action_item2 -> selectedFragment = ItemTwoFragment.newInstance()
                R.id.action_item3 -> selectedFragment = ItemThreeFragment.newInstance()
            }
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, selectedFragment)
            transaction.commit()
            true
        }

        //Manually displaying the first fragment - one time only
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, ItemOneFragment.newInstance())
        transaction.commit()

        //Handle intents, for launcher shortcuts
        val iin = intent
        val bnd = iin.extras

        //If intent extras exist
        if (bnd != null) {
            // Create a string 'intentArgument' based on intent extra 'TEXT'
            val intentArgument = bnd.get("android.intent.extra.TEXT") as String
            // Run async call to intentArgument URL
            RetrieveFeedTask(intentArgument).execute()
        }

    }

    //BASIC FUNCTIONALITY
    //Show a snack, with message passed as an argument
    fun showSnack(message: String) =
            Snackbar.make(findViewById<View>(R.id.placeSnackBar), message, Snackbar.LENGTH_LONG)
                    .show()

    // Return string of content returned from a URL
    fun getFromURL(url_string: String): String? {
        return try {
            URL(url_string).readText() //
        } catch (e: Exception) {
            // TODO: Actually handle error properly
            // Log any errors
            Log.e("ERROR", e.message, e)
            //If connection failed, return null string
            null
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
                RetrieveFeedTask("status/clear").execute()
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

    //BASIC EMPTY RETREIVEFEED CLASS (ONLY USED FOR CLEARING, SHUTDOWN ETC WHERE NO GUI NEEDED)
    internal inner class RetrieveFeedTask(private val api_arg: String) : AsyncTask<Void, Void, String>() {

        //Before executing asynctask
        override fun onPreExecute() {
            //Show progressbars, hide content
        }

        //Main asynctask
        override fun doInBackground(vararg params: Void): String? {
            return getFromURL(apiBase + api_arg)
        }

        //After executing asynctask
        override fun onPostExecute(response: String?) {
            if (response == null) {
                showSnack("Error when parsing response")
            } else {
                //Call function to handle response string, only if response not null
                //Log response to debug terminal
                Log.i("INFO", response)
            }

        }
    }

}
