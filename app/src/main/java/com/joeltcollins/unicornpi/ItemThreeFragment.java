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

package com.joeltcollins.unicornpi;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;



public class ItemThreeFragment extends Fragment {
    public static ItemThreeFragment newInstance() {
        ItemThreeFragment fragment = new ItemThreeFragment();
        return fragment;
    }

    //INITIATE CORE UI ELEMENTS FOR ACCESS IN ALL FUNCTIONS WITHIN THIS CLASS, BUT NOT CHILD CLASSES (which are handled instead by the rootview argument)
    Spinner rainbow_theme_spinner, alsa_theme_spinner;
    SeekBar rainbow_speed_seekbar, alsa_sensitivity_seekbar, alsa_mic_seekbar, alsa_vol_seekbar;

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Get current view
        final View v = inflater.inflate(R.layout.fragment_item_three, container, false);

        //Get core UI elements to be updated by asynctask
        this.rainbow_theme_spinner = (Spinner) v.findViewById(R.id.rainbow_theme_spinner);
        this.rainbow_speed_seekbar = (SeekBar) v.findViewById(R.id.rainbow_speed_seekbar);
        this.alsa_theme_spinner = (Spinner) v.findViewById(R.id.alsa_theme_spinner);
        this.alsa_sensitivity_seekbar = (SeekBar) v.findViewById(R.id.alsa_sensitivity_seekbar);
        this.alsa_mic_seekbar = (SeekBar) v.findViewById(R.id.alsa_mic_seekbar);
        this.alsa_vol_seekbar = (SeekBar) v.findViewById(R.id.alsa_vol_seekbar);

        //INITIATE RAINBOW START BUTTON
        Button rainbow_start_button = (Button) v.findViewById(R.id.rainbow_start_button);
        //RAINBOW START BUTTON LISTENER & FUNCTIONS
        rainbow_start_button.setOnClickListener(new View.OnClickListener() {
            //Get main activity
            MainActivity activity = (MainActivity) getActivity();

            @Override
            public void onClick(View view)
            {
                int spinner_position = rainbow_theme_spinner.getSelectedItemPosition();
                int speed_pc= rainbow_speed_seekbar.getProgress();
                float speed = (speed_pc+1)/(float)100;

                new ItemThreeFragment.RetrieveFeedTask(v, "rainbow/set?mode="+String.valueOf(spinner_position)+"&speed="+String.valueOf(speed)+"&status=1",true).execute();

            }
        });

        //INITIATE ALSA START BUTTON
        Button alsa_start_button = (Button) v.findViewById(R.id.alsa_start_button);
        //ALSA START BUTTON LISTENER & FUNCTIONS
        alsa_start_button.setOnClickListener(new View.OnClickListener() {

            //Get main activity
            MainActivity activity = (MainActivity) getActivity();

            @Override
            public void onClick(View view)
            {
                int spinner_position = alsa_theme_spinner.getSelectedItemPosition();
                int sensitivity = alsa_sensitivity_seekbar.getProgress()/10;
                int mic = alsa_mic_seekbar.getProgress();
                int vol = alsa_vol_seekbar.getProgress();

                new ItemThreeFragment.RetrieveFeedTask(v, "alsa/set?mode="+String.valueOf(spinner_position)+"&sensitivity="+String.valueOf(sensitivity)+"&monitor="+String.valueOf(mic)+"&volume="+String.valueOf(vol)+"&status=1",true).execute();

            }
        });


        //GET API RESPONSE FOR UI STARTUP
        new ItemThreeFragment.RetrieveFeedTask(v, "status/all",true).execute();

        return v;
    }


    //RETREIVEFEED CLASS
    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        //Set up rootview argument
        private View rootView;

        //Set up core UI references
        private RelativeLayout progressBar;
        private ScrollView mainLayout;

        //Handle preferences and API URL
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String device_ip = pref.getString("prefs_device_ip", "0.0.0.0");
        String device_port = pref.getString("prefs_device_port", "5000");
        String api_version = pref.getString("prefs_api_version", "51.0");
        String api_root="http://"+device_ip+":"+device_port+"/api/"+api_version+"/";

        //Initiate API argument string
        private String api_arg;

        //Get mainactivity for sending snackbars etc
        MainActivity activity = (MainActivity) getActivity();

        //Set up variable for argument determining if progressbar should show
        private boolean show_progress;

        //Set up the function to allow arguments to be passed
        public RetrieveFeedTask(View rootView, String api_argument, boolean progress_bar){
            super();

            //Grab rootview from argument, to set up core UI elements
            this.rootView=rootView;

            //Set up core UI elements
            this.progressBar = (RelativeLayout) rootView.findViewById(R.id.anim_progressLayout);
            this.mainLayout = (ScrollView) rootView.findViewById(R.id.anim_mainLayout);

            //Get API argument from asynctask call argument
            api_arg = api_argument;

            //Get argument of if UI should be blocked by progress bar while executing
            show_progress = progress_bar;
        }

        //Before executing asynctask
        protected void onPreExecute() {
            //Show progressbars, hide content
            if (show_progress == true) {
                this.progressBar.setVisibility(View.VISIBLE);
                this.mainLayout.setVisibility(View.GONE);
            }
        }

        //Main asynctask
        protected String doInBackground(Void... params) {
            try {
                //Set up URL from arguments and root
                URL url = new URL(api_root+api_arg);
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

        //After executing asynctask
        protected void onPostExecute(String response) {
            if(response == null) {
                activity.showSnack("Error when parsing response");
            }

            else {
                //Call function to handle response string, only if response not null
                HandleResponse(response);
                //Log response to debug terminal
                Log.i("INFO", response);
            }

            //Hide progressbar, show content
            if (show_progress==true) {
                this.progressBar.setVisibility(View.GONE);
                this.mainLayout.setVisibility(View.VISIBLE);
            }

        }
    }


    //HANDLE JSON RESPONSE
    private void HandleResponse(String response){
        boolean update_brightness_slider = false;
        try {
            JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
            if (object.has("dynamic_rainbow_mode")){
                int response_rainbow_mode = object.getInt("dynamic_rainbow_mode");
                rainbow_theme_spinner.setSelection(response_rainbow_mode);
            }
            if (object.has("dynamic_rainbow_speed")){
                String response_rainbow_speed = object.getString("dynamic_rainbow_speed");
                int speed_pc = Math.round(Float.parseFloat(response_rainbow_speed)*100) -1;
                rainbow_speed_seekbar.setProgress(speed_pc );
            }
            if (object.has("dynamic_alsa_mode")){
                int response_alsa_mode = object.getInt("dynamic_alsa_mode");
                alsa_theme_spinner.setSelection(response_alsa_mode);
            }
            if (object.has("dynamic_alsa_sensitivity")){
                int response_alsa_sensitivity = object.getInt("dynamic_alsa_sensitivity");
                alsa_sensitivity_seekbar.setProgress(response_alsa_sensitivity*10);
            }
            if (object.has("dynamic_alsa_monitor")){
                int response_alsa_mic = object.getInt("dynamic_alsa_monitor");
                alsa_mic_seekbar.setProgress(response_alsa_mic);
            }
            if (object.has("dynamic_alsa_volume")){
                int response_alsa_vol = object.getInt("dynamic_alsa_volume");
                alsa_vol_seekbar.setProgress(response_alsa_vol);
            }

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
