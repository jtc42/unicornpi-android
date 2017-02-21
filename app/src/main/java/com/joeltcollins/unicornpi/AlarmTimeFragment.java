package com.joeltcollins.unicornpi;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.app.DialogFragment;
import android.app.Dialog;
import java.util.Calendar;
import android.widget.TimePicker;



public class AlarmTimeFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){

        //Set up the textview object to grab the currently set time
        TextView alarm_time_text = (TextView) getActivity().findViewById(R.id.alarm_time_text);

        //Use the current time as the default values for the time picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        //Get current time value from the textview
        String current_time = alarm_time_text.getText().toString();
        String[] parts = current_time.split(":");
        int current_hours = Integer.parseInt(parts[0]);
        int current_mins = Integer.parseInt(parts[1]);

        //Create and return a new instance of TimePickerDialog
        return new TimePickerDialog(getActivity(),this, current_hours, current_mins, DateFormat.is24HourFormat(getActivity()));
    }

    //onTimeSet() callback method
    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
        //Do something with the user chosen time
        //Get reference of host activity (XML Layout File) TextView widget
        TextView alarm_time_text = (TextView) getActivity().findViewById(R.id.alarm_time_text);

        String formatted_time = String.format("%02d", hourOfDay)+ ":" + String.format("%02d", minute);

        //Set time text
        alarm_time_text.setText(formatted_time);
    }
}