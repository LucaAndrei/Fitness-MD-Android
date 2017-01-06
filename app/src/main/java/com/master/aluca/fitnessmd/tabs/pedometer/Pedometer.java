package com.master.aluca.fitnessmd.tabs.pedometer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;
import android.widget.TextView;

import com.master.aluca.fitnessmd.ArcProgress;
import com.master.aluca.fitnessmd.Constants;
import com.master.aluca.fitnessmd.FitnessMD_Service;
import com.master.aluca.fitnessmd.MainActivity;
import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.util.IStepNotifier;
import com.master.aluca.fitnessmd.util.SharedPreferencesManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by aluca on 11/3/16.
 */
public class Pedometer {

    private static final String LOG_TAG = "Fitness_Pedometer";

    private Context mContext = null;
    private static MainActivity mMainActivity;
    private SharedPreferencesManager sharedPreferencesManager;
    static TextView tvKCal, tvKm, tvDateToday;
    private FitnessMD_Service mService;
    private ArcProgress mArcProgress;
    private int stepsForCurrentDay;
    private int totalSteps = 0;
    Chronometer timeElapsed;
    int hours,minutes,seconds;
    double kilometers;
    double caloriesBurned = 0.0d;

    public Pedometer(Context context, MainActivity mainActivity) {
        Log.d(LOG_TAG,"Pedometer");
        mContext = context;
        mMainActivity = mainActivity;
        sharedPreferencesManager = SharedPreferencesManager.getInstance(context);

        totalSteps = sharedPreferencesManager.getStepsForCurrentDay();

        Log.d(LOG_TAG, "constructor totalSteps : " + totalSteps);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.STEP_INCREMENT_INTENT);
        intentFilter.addAction(Constants.CONNECTED_DEVICE_DETAILS_INTENT);
        intentFilter.addAction(Constants.DEVICE_CONNECTION_LOST);
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive : " + intent.getAction());
            if (intent.getAction() == Constants.STEP_INCREMENT_INTENT) {
                Log.d(LOG_TAG, "STEP_INCREMENT_INTENT received");
                int steps = intent.getIntExtra(Constants.STEP_INCREMENT_BUNDLE_KEY,-1);
                totalSteps += steps;
                Log.d(LOG_TAG, "totalSteps : " + totalSteps);
                mArcProgress.setProgress(totalSteps);
                setKm();
                setKCal();
            } else if (intent.getAction().equals(Constants.CONNECTED_DEVICE_DETAILS_INTENT)) {
                //startTimer();

                //TODO  - cand iesi din aplicatie si intri iar, timerul ramane setat pe 0 si nu mai porneste.

                if(!sharedPreferencesManager.getChronometerRunning()) {
                    sharedPreferencesManager.setChronometerBase(SystemClock.elapsedRealtime());
                }
                timeElapsed.setBase(sharedPreferencesManager.getChronometerBase());
                timeElapsed.start();
                sharedPreferencesManager.setChronometerRunning(true);
            } else if (intent.getAction().equals(Constants.DEVICE_CONNECTION_LOST)) {
                sharedPreferencesManager.setChronometerBase(SystemClock.elapsedRealtime());
                sharedPreferencesManager.setChronometerRunning(false);
                timeElapsed.stop();
            }
        }
    };

    public void setup() {
        tvKCal = (TextView) mMainActivity.findViewById(R.id.tvKCal);
        tvKm = (TextView) mMainActivity.findViewById(R.id.tvKm);
        tvDateToday = (TextView) mMainActivity.findViewById(R.id.tvDateToday);
        SimpleDateFormat s = new SimpleDateFormat("d MMMM yyyy");
        tvDateToday.setText(s.format(new Date()));
        mArcProgress = (ArcProgress) mMainActivity.findViewById(R.id.arc_progress_pedometer);
        mArcProgress.setProgress(sharedPreferencesManager.getStepsForCurrentDay());

        timeElapsed = (Chronometer) mMainActivity.findViewById(R.id.chronometer);
        timeElapsed.setOnChronometerTickListener(new OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                Log.d(LOG_TAG,"onChronoTick");
                long time = SystemClock.elapsedRealtime() - chronometer.getBase();
                hours = (int) (time / 3600000);
                minutes = (int) (time - hours * 3600000) / 60000;
                seconds = (int) (time - hours * 3600000 - minutes * 60000) / 1000;
                String hh = hours < 10 ? "0" + hours : hours + "";
                String mm = minutes < 10 ? "0" + minutes : minutes + "";
                String ss = seconds < 10 ? "0" + seconds : seconds + "";
                chronometer.setText(hh + ":" + mm + ":" + ss);
            }
        });

        setKm();
        setKCal();

    }

    public void setKm() {
        kilometers = (double)totalSteps / 1320;
        Log.d(LOG_TAG, "kilometers : " + kilometers);
        kilometers = Math.round(kilometers * 10d) / 10d;
        tvKm.setText("" + kilometers);
    }

    /*
            For 0% grade:

    CB = [0.0215 x KPH3 - 0.1765 x KPH2 + 0.8710 x KPH + 1.4577] x WKG x T


http://www.shapesense.com/fitness-exercise/calculators/walking-calorie-burn-calculator.shtml
     */
    public void setKCal() {
        double timeActiveInHours = (double)((hours * 60) + minutes) / 60;
        double kilometersPerHour = kilometers / timeActiveInHours;
        kilometersPerHour = Math.round(kilometersPerHour * 10d) / 10d;
        if (kilometersPerHour > 1.0) {
            double kph3 = 0.0215 * (Math.pow(kilometersPerHour,3));
            double kph2 = 0.1765 * (Math.pow(kilometersPerHour,2));
            double kph = 0.8710 * kilometersPerHour;
            timeActiveInHours = Math.round(timeActiveInHours * 10d) / 10d;

            kph3 = Math.round(kph3 * 10d) / 10d;
            kph2 = Math.round(kph2 * 10d) / 10d;
            kph = Math.round(kph * 10d) / 10d;
            caloriesBurned = (kph3 - kph2 + kph + 1.4577) * sharedPreferencesManager.getWeight() * timeActiveInHours;
        }
        tvKCal.setText("" + (int)caloriesBurned);
    }

    /*

    Activity Multiplier (Both HB + KA Method use same activity multiplier)
    Little or No Exercise, Desk Job 	                1.2 x BMR
    Light Exercise, Sports 1 to 3 Times Per Week 	    1.375 x BMR
    Moderate Exercise, Sports 3 to 5 Times Per Week     1.55 x BMR
    Heavy Exercise, Sports 6 to 7 Times Per Week 	    1.725 x BMR


     */

    public int getStepsForCurrentDay() {
        return totalSteps;
    }
}