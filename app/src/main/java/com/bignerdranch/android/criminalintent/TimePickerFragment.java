package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by andro1d on 06.02.2017.
 */

public class TimePickerFragment extends DialogFragment {

    private static final String ARG_TIME = "time";
    public TimePicker mTimePicker;

    int year = 0;
    int month = 0;
    int day = 0;


    public static final String EXTRA_TIME =
            "com.bignerdranch.android.criminalintent.time";


    public static TimePickerFragment newInstance (Date date){
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME,date);


        TimePickerFragment tpf = new TimePickerFragment();
        tpf.setArguments(args);
        return tpf;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Date date = (Date) getArguments().getSerializable(ARG_TIME);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        final int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);


        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time,null);
        mTimePicker = (TimePicker)v.findViewById(R.id.dialog_date_time_picker);
        mTimePicker.setCurrentHour(hour);
        mTimePicker.setCurrentMinute(minute);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("time enter")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int hours = mTimePicker.getCurrentHour();
                        int minutes = mTimePicker.getCurrentMinute();
                        Date date = new GregorianCalendar(year, month, day, hours, minutes).getTime();
                        sendResult(Activity.RESULT_OK, date);
                    }
                })
                .create();
        //ЗАКОНЧИЛ ЗДЕСЬ - делал УПРАЖНение по дополнению кнопки для timePicker. диалог запускается
        //проставляется нужное время. На этом всё. Нужно передавать обновленное время обратно в активити.

    }

    private void sendResult (int resultCode, Date time){

        if (getTargetFragment()== null){
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_TIME, time);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);

    }
}
