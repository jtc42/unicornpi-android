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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.util.Log;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;



public class ItemTwoFragment extends Fragment {
    public static ItemTwoFragment newInstance() {
        ItemTwoFragment fragment = new ItemTwoFragment();
        return fragment;
    }

    //INITIATE CORE UI ELEMENTS FOR ACCESS IN ALL FUNCTIONS WITHIN THIS CLASS, BUT NOT CHILD CLASSES (which are handled instead by the rootview argument)
    ColorPickerView colorPickerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Get current view
        final View v = inflater.inflate(R.layout.fragment_item_two, container, false);


        //INITIATE COLOR PICKER (find in current view)
        this.colorPickerView = (ColorPickerView) v.findViewById(R.id.color_picker_view);
        //COLOR PICKER LISTENER
        colorPickerView.addOnColorSelectedListener(new OnColorSelectedListener() {
            @Override
            public void onColorSelected(int selectedColor) {

                //Find main activity (used to call main activity functions)
                MainActivity activity = (MainActivity) getActivity();

                //RGB hex string of selected color
                String hex_string = Integer.toHexString(selectedColor).toUpperCase().substring(2, 8);
                new ItemTwoFragment.RetrieveFeedTask(v, "clamp/set?hex=" + hex_string + "&status=1", false).execute();
            }
        });


        //INITIATE NIGHT START BUTTON
        Button clamp_button_night = (Button) v.findViewById(R.id.clamp_button_night);
        //ALARM START BUTTON LISTENER & FUNCTIONS
        clamp_button_night.setOnClickListener(new View.OnClickListener() {

            //Get main activity
            MainActivity activity = (MainActivity) getActivity();

            @Override
            public void onClick(View view)
            {
                new ItemTwoFragment.RetrieveFeedTask(v, "clamp/set?hex=ff880d&status=1", false).execute();
                //activity.showSnack("Night lamp started");
            }
        });

        //INITIATE EVENING START BUTTON
        Button clamp_button_evening = (Button) v.findViewById(R.id.clamp_button_evening);
        //ALARM START BUTTON LISTENER & FUNCTIONS
        clamp_button_evening.setOnClickListener(new View.OnClickListener() {

            //Get main activity
            MainActivity activity = (MainActivity) getActivity();

            @Override
            public void onClick(View view)
            {
                new ItemTwoFragment.RetrieveFeedTask(v, "clamp/set?hex=ff9f46&status=1", false).execute();
                //activity.showSnack("Evening lamp started");
            }
        });

        //INITIATE DESK START BUTTON
        Button clamp_button_desk = (Button) v.findViewById(R.id.clamp_button_desk);
        //ALARM START BUTTON LISTENER & FUNCTIONS
        clamp_button_desk.setOnClickListener(new View.OnClickListener() {

            //Get main activity
            MainActivity activity = (MainActivity) getActivity();

            @Override
            public void onClick(View view)
            {
                new ItemTwoFragment.RetrieveFeedTask(v, "clamp/set?hex=ffc08c&status=1", false).execute();
                //activity.showSnack("Desk lamp started");
            }
        });

        //INITIATE DAY START BUTTON
        Button clamp_button_day = (Button) v.findViewById(R.id.clamp_button_day);
        //ALARM START BUTTON LISTENER & FUNCTIONS
        clamp_button_day.setOnClickListener(new View.OnClickListener() {

            //Get main activity
            MainActivity activity = (MainActivity) getActivity();

            @Override
            public void onClick(View view)
            {
                new ItemTwoFragment.RetrieveFeedTask(v, "clamp/set?hex=ffe4cd&status=1", false).execute();
                //activity.showSnack("Day lamp started");
            }
        });



        //GET API RESPONSE FOR UI STARTUP
        new ItemTwoFragment.RetrieveFeedTask(v, "status/all",true).execute();

        return v;
    }



    //RETREIVEFEED CLASS
    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        //Set up rootview argument
        private View rootView;

        //Set up core UI references
        private RelativeLayout progressBar;
        private RelativeLayout mainLayout;

        //Handle preferences and API URL
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String device_ip = pref.getString("prefs_device_ip", "0.0.0.0");
        String device_port = pref.getString("prefs_device_port", "5000");
        String api_version = pref.getString("prefs_api_version", "1.0");
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
            this.progressBar = (RelativeLayout) rootView.findViewById(R.id.clamp_progressLayout);
            this.mainLayout = (RelativeLayout) rootView.findViewById(R.id.clamp_mainLayout);

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
        try {
            JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
            //If global status is returned, route music have been status/all, so brightness slider should be updated
            if (object.has("static_clamp_hex")){
                String response_clamp_hex = object.getString("static_clamp_hex");

                int color_int = (int) Long.parseLong("ff"+response_clamp_hex, 16);
                colorPickerView.setColor(color_int, false);

            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
