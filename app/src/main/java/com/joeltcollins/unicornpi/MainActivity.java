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

package com.joeltcollins.unicornpi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    //ONCREATE FUNCTIONS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //General set up stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up bottom navigation bar and populate
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener
                (new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        Fragment selectedFragment = null;
                        switch (item.getItemId()) {
                            case R.id.action_item1:
                                selectedFragment = ItemOneFragment.newInstance();
                                break;
                            case R.id.action_item2:
                                selectedFragment = ItemTwoFragment.newInstance();
                                break;
                            case R.id.action_item3:
                                selectedFragment = ItemThreeFragment.newInstance();
                                break;
                        }
                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.frame_layout, selectedFragment);
                        transaction.commit();
                        return true;
                    }
                });

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, ItemOneFragment.newInstance());
        transaction.commit();

        //Handle intents, for launcher shortcuts
        Intent iin= getIntent();
        Bundle bnd = iin.getExtras();

        if(bnd!=null) //If intent extras exist
        {
            String intent_arg =(String) bnd.get("android.intent.extra.TEXT"); //Create a string 'intent_arg' based on intent extra 'TEXT'
            if(intent_arg != null) { //If intent extra text is not empty
                new RetrieveFeedTask(intent_arg).execute();
            }
        }

    }

    //BASIC FUNCTIONALITY
    //Show a snack, with message passed as an argument
    public void showSnack(String message) {
        Snackbar.make(findViewById(R.id.placeSnackBar), message, Snackbar.LENGTH_LONG)
                .show();
    }

    // Generate the API base URL from preferences
    public String getAPIBase() {
        //Handle preferences and API URL
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

        String device_ip = pref.getString("prefs_device_ip", "0.0.0.0");
        String device_port = pref.getString("prefs_device_port", "5000");
        String api_version = pref.getString("prefs_api_version", "1.0");
        return "http://"+device_ip+":"+device_port+"/api/"+api_version+"/";
    }

    // Return string of content returned from a URL
    public String getFromURL(String url_string) {
        try {
            //Set up URL from arguments and root
            URL url = new URL(url_string);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                //If connection successful, return obtained JSON string
                return stringBuilder.toString();
            }
            finally{
                //Close connection
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            //If connection failed, return null string
            return null;
        }
    }

    //GUI SETUP FUNCTIONS
    //Expand the main menu into the menu bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    //Map menu items to functions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_main_clear:
                new RetrieveFeedTask("status/clear").execute();
                return true;
            case R.id.menu_main_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            case R.id.menu_main_about:
                String versionName = BuildConfig.VERSION_NAME;
                showSnack("Unicorn Pi for Android, version "+ versionName);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //BASIC EMPTY RETREIVEFEED CLASS (ONLY USED FOR CLEARING, SHUTDOWN ETC WHERE NO GUI NEEDED)
    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        //Initiate API argument string
        private final String api_arg;

        //Set up the function to allow arguments to be passed
        RetrieveFeedTask(String api_argument){
            super();
            //Get API argument from asynctask call argument
            api_arg = api_argument;

        }

        //Before executing asynctask
        protected void onPreExecute() {
            //Show progressbars, hide content
        }

        //Main asynctask
        protected String doInBackground(Void... params) {
            return getFromURL(getAPIBase() + api_arg);
        }

        //After executing asynctask
        protected void onPostExecute(String response) {
            if(response == null) {
                showSnack("Error when parsing response");
            }

            else {
                //Call function to handle response string, only if response not null
                //Log response to debug terminal
                Log.i("INFO", response);
            }

        }
    }

}
