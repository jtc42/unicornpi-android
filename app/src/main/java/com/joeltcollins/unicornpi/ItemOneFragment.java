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

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.os.AsyncTask;


public class ItemOneFragment extends Fragment {
    public static ItemOneFragment newInstance() {
        return new ItemOneFragment();
    }

    //INITIATE CORE UI ELEMENTS FOR ACCESS IN ALL FUNCTIONS WITHIN THIS CLASS, BUT NOT CHILD CLASSES (which are handled instead by the rootview argument)
    private SeekBar brightness_seekbar;
    private SeekBar fade_target_seekbar;
    private TextView alarm_time_text;
    private TextView alarm_status;
    private TextView fade_status;
    private EditText alarm_lead;
    private EditText alarm_tail;
    private EditText fade_time;
    //Grab resources
    private String alarm_status_active;
    private String alarm_status_inactive;
    private String fade_status_active;
    private String fade_status_inactive;
    private int status_active;
    private int status_inactive;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Get current view
        final View v = inflater.inflate(R.layout.fragment_item_one, container, false);

        //Get core UI elements to be updated by asynctask
        this.brightness_seekbar = v.findViewById(R.id.brightness_seekbar);
        this.alarm_time_text = v.findViewById(R.id.alarm_time_text);
        this.alarm_lead = v.findViewById(R.id.alarm_lead);
        this.alarm_tail = v.findViewById(R.id.alarm_tail);
        this.alarm_status = v.findViewById(R.id.alarm_status);
        this.fade_time = v.findViewById(R.id.fade_time);
        this.fade_target_seekbar = v.findViewById(R.id.fade_target_seekbar);
        this.fade_status = v.findViewById(R.id.fade_status);
        //Get resources
        this.alarm_status_active = getString(R.string.alarm_status_active);
        this.alarm_status_inactive = getString(R.string.alarm_status_inactive);
        this.fade_status_active = getString(R.string.fade_status_active);
        this.fade_status_inactive = getString(R.string.fade_status_inactive);
        this.status_active = ContextCompat.getColor(getContext(), R.color.label_active);
        this.status_inactive = ContextCompat.getColor(getContext(), R.color.label_inactive);


        //Initiate any remaining brightness controls
        final TextView brightness_text = v.findViewById(R.id.brightness_text);
        //BRIGHTNESS SEEK LISTENER & FUNCTIONS
        brightness_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                brightness_text.setText(Integer.toString(progress));
                if (fromUser) { //Blocks API call if UI is just updating (caused fades to stop on app load)
                    new RetrieveFeedTask(v, "brightness/set?val=" + String.valueOf(progress), false).execute();
                }
                if (fade_status.getText().toString().equals(fade_status_active)){
                    fade_status.setText(fade_status_inactive);
                    fade_status.setTextColor(status_inactive);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //activity.showSnack("Started seek");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                new RetrieveFeedTask(v, "brightness/set?val=" + String.valueOf(progress), false).execute();
                //activity.showSnack("Brightness set");
            }
        });

        //INITIATE FADE START BUTTON
        Button fade_start_button = v.findViewById(R.id.fade_start_button);
        //ALARM START BUTTON LISTENER & FUNCTIONS
        fade_start_button.setOnClickListener(new View.OnClickListener() {

            //Get main activity
            final MainActivity activity = (MainActivity) getActivity();

            @Override
            public void onClick(View view)
            {
                activity.showSnack("Fade started");
                int minutes= Integer.parseInt(fade_time.getText().toString());
                float target = fade_target_seekbar.getProgress()/(float)100;

                new RetrieveFeedTask(v, "fade/set?minutes=" + String.valueOf(minutes) + "&target=" + String.valueOf(target) + "&status=1", false).execute();
            }
        });

        //INITIATE FADE STOP BUTTON
        Button fade_stop_button = v.findViewById(R.id.fade_stop_button);
        //ALARM START BUTTON LISTENER & FUNCTIONS
        fade_stop_button.setOnClickListener(new View.OnClickListener() {

            //Get main activity
            final MainActivity activity = (MainActivity) getActivity();

            @Override
            public void onClick(View view)
            {
                activity.showSnack("Fade stopped");
                new RetrieveFeedTask(v, "fade/set?&status=0", false).execute();
            }
        });

        //INITIATE ALARM TIME BUTTON
        Button alarm_time_button = v.findViewById(R.id.alarm_time_button);
        //ALARM TIME BUTTON LISTENER & FUNCTIONS
        alarm_time_button.setOnClickListener(new View.OnClickListener() {

            //Get main activity
            final MainActivity activity = (MainActivity) getActivity();

            @Override
            public void onClick(View view)
            {
                //Start up timepicker fragment
                //NOTE: ONTIMECHANGE IS HANDLES BY THE ALARMTIMEFRAGMENT
                //DRAWING TO UI SHOULD BE HANDLED THERE
                DialogFragment newFragment = new AlarmTimeFragment();
                newFragment.show(activity.getFragmentManager(),"TimePicker");
            }
        });

        //INITIATE ALARM START BUTTON
        Button alarm_start_button = v.findViewById(R.id.alarm_start_button);
        //ALARM START BUTTON LISTENER & FUNCTIONS
        alarm_start_button.setOnClickListener(new View.OnClickListener() {

            //Get main activity
            final MainActivity activity = (MainActivity) getActivity();

            @Override
            public void onClick(View view)
            {
                activity.showSnack("Alarm set");
                int lead = Integer.parseInt(alarm_lead.getText().toString());
                int tail = Integer.parseInt(alarm_tail.getText().toString());
                String time = alarm_time_text.getText().toString();

                new RetrieveFeedTask(v, "alarm/set?lead=" + String.valueOf(lead) + "&tail=" + String.valueOf(tail) + "&time=" + time + "&status=1", false).execute();
            }
        });

        //INITIATE FADE STOP BUTTON
        Button alarm_stop_button = v.findViewById(R.id.alarm_stop_button);
        //ALARM START BUTTON LISTENER & FUNCTIONS
        alarm_stop_button.setOnClickListener(new View.OnClickListener() {

            //Get main activity
            final MainActivity activity = (MainActivity) getActivity();

            @Override
            public void onClick(View view)
            {
                activity.showSnack("Alarm unset");
                new RetrieveFeedTask(v, "alarm/set?&status=0", false).execute();
            }
        });

        //GET API RESPONSE FOR UI STARTUP
        new RetrieveFeedTask(v, "status/all",true).execute();

        return v;
    }

    //RETREIVEFEED CLASS
    class RetrieveFeedTask extends AsyncTask<Void, Void, String> {

        //Set up core UI references
        private final RelativeLayout progressBar;
        private final ScrollView mainLayout;

        //Initiate API argument string
        private final String api_arg;

        //Get mainactivity for sending snackbars etc
        final MainActivity activity = (MainActivity) getActivity();

        //Set up variable for argument determining if progressbar should show
        private final boolean show_progress;

        //Set up the function to allow arguments to be passed
        RetrieveFeedTask(View rootView, String api_argument, boolean progress_bar){
            super();
            //Set up core UI elements
            this.progressBar = rootView.findViewById(R.id.system_progressLayout);
            this.mainLayout = rootView.findViewById(R.id.system_mainLayout);

            //Get API argument from asynctask call argument
            api_arg = api_argument;

            //Get argument of if UI should be blocked by progress bar while executing
            show_progress = progress_bar;
        }

        //Before executing asynctask
        protected void onPreExecute() {
            //Show progressbars, hide content
            if (show_progress) {
                this.progressBar.setVisibility(View.VISIBLE);
                this.mainLayout.setVisibility(View.GONE);
            }
        }

        //Main asynctask
        protected String doInBackground(Void... params) {
            return activity.getFromURL(activity.getAPIBase() + api_arg);
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
            if (show_progress) {
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
            //If global status is returned, route music have been status/all, so brightness slider should be updated
            if (object.has("global_status")){
                update_brightness_slider = true;
            }
            if (object.has("global_brightness_val")){
                int response_brightness_val = object.getInt("global_brightness_val");
                if (update_brightness_slider) {
                    brightness_seekbar.setProgress(response_brightness_val);
                }
            }
            if (object.has("special_alarm_time")){
                String alarm_time = object.getString("special_alarm_time");

                String[] parts = alarm_time.split(":");
                int current_hours = Integer.parseInt(parts[0]);
                int current_mins = Integer.parseInt(parts[1]);

                String formatted_time = String.format("%02d", current_hours)+ ":" + String.format("%02d", current_mins);

                alarm_time_text.setText(formatted_time);
            }
            if (object.has("special_alarm_lead")){
                int response_alarm_lead = object.getInt("special_alarm_lead");
                alarm_lead.setText(String.valueOf(response_alarm_lead));
            }
            if (object.has("special_alarm_tail")){
                int response_alarm_tail = object.getInt("special_alarm_tail");
                alarm_tail.setText(String.valueOf(response_alarm_tail));
            }
            if (object.has("special_alarm_status")){
                int response_alarm_status = object.getInt("special_alarm_status");
                if (response_alarm_status==1){
                    alarm_status.setText(alarm_status_active);
                    alarm_status.setTextColor(status_active);
                }
                else {
                    alarm_status.setText(alarm_status_inactive);
                    alarm_status.setTextColor(status_inactive);
                }
            }
            if (object.has("special_fade_minutes")){
                int response_fade_minutes = object.getInt("special_fade_minutes");
                fade_time.setText(String.valueOf(response_fade_minutes));
            }
            if (object.has("special_fade_target")){
                String response_fade_target = object.getString("special_fade_target");
                int fade_percent = Math.round(Float.parseFloat(response_fade_target)*100);
                fade_target_seekbar.setProgress(fade_percent);
            }
            if (object.has("special_fade_status")){
                int response_alarm_status = object.getInt("special_fade_status");
                if (response_alarm_status==1){
                    fade_status.setText(fade_status_active);
                    fade_status.setTextColor(status_active);
                }
                else {
                    fade_status.setText(fade_status_inactive);
                    fade_status.setTextColor(status_inactive);
                }
            }

        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

}
