package com.master.aluca.fitnessmd.tabs.scale;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
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
public class Scale {

    private static final String LOG_TAG = "Fitness_Scale";

    private Context mContext = null;
    private static MainActivity mMainActivity;
    private SharedPreferencesManager sharedPreferencesManager;
    static TextView tvDate, tvLastMeasurement;
    private FitnessMD_Service mService;
    private ArcProgress mArcProgressScale;

    public Scale(Context context, MainActivity mainActivity) {
        Log.d(LOG_TAG,"Scale");
        mContext = context;
        mMainActivity = mainActivity;
        sharedPreferencesManager = SharedPreferencesManager.getInstance(context);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.WEIGHT_RECEIVED_INTENT);
        intentFilter.addAction(Constants.WEIGHT_GOAL_INTENT);
        mContext.registerReceiver(mBroadcastReceiver, intentFilter);
    }


    /*
            TODO - this receiver should be unregistered when the application is destroyed
     */
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive : " + intent.getAction());
            if (intent.getAction().equalsIgnoreCase(Constants.WEIGHT_RECEIVED_INTENT)) {
                Log.d(LOG_TAG, "WEIGHT_RECEIVED_INTENT received");
                float weight = intent.getFloatExtra(Constants.WEIGHT_RECEIVED_WEIGHT_BUNDLE_KEY, -1);
                if (weight != -1) {
                    Log.d(LOG_TAG, "weight : " + weight);
                    if (mArcProgressScale != null) {
                        mArcProgressScale.setProgressWeight(weight);
                    }
                } else {
                    Log.d(LOG_TAG, "weight ERROR");
                }

                long lastMeasurementDay = intent.getLongExtra(Constants.WEIGHT_RECEIVED_LAST_MSRMNT_BUNDLE_KEY, -1);
                if (lastMeasurementDay != -1) {
                    Log.d(LOG_TAG, "lastMeasurementDay : " + (new Date(lastMeasurementDay)));
                    SimpleDateFormat s = new SimpleDateFormat("d MMMM yyyy");
                    tvDate.setText(s.format(new Date(lastMeasurementDay)));
                } else {
                    Log.d(LOG_TAG, "lastMeasurementDay ERROR");
                }

            } else if(intent.getAction().equalsIgnoreCase(Constants.WEIGHT_GOAL_INTENT)) {
                Log.d(LOG_TAG, "WEIGHT_GOAL_INTENT received");
                float weightGoal = intent.getFloatExtra(Constants.WEIGHT_GOAL_BUNDLE_KEY, -1);
                if (weightGoal != -1) {
                    Log.d(LOG_TAG, "weightGoal : " + weightGoal);
                    if (mArcProgressScale != null) {
                        mArcProgressScale.setBottomText("Goal: " + weightGoal + " kg");
                    }
                } else {
                    Log.d(LOG_TAG, "weightGoal ERROR");
                }
            }
        }
    };

    public void setup() {
        tvDate = (TextView) mMainActivity.findViewById(R.id.tvDate);
        tvLastMeasurement = (TextView) mMainActivity.findViewById(R.id.tvLastMeasurement);
        mArcProgressScale = (ArcProgress) mMainActivity.findViewById(R.id.arc_progress_scale);
        float weightGoal = sharedPreferencesManager.getWeightGoal();
        mArcProgressScale.setBottomText("Goal: " + weightGoal + " kg");

        float weight = sharedPreferencesManager.getWeight();
        mArcProgressScale.setProgressWeight(weight);

        SimpleDateFormat s = new SimpleDateFormat("d MMMM yyyy");
        tvDate.setText(s.format(new Date(System.currentTimeMillis())));
    }
}
