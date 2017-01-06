package com.master.aluca.fitnessmd.tabs;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.master.aluca.fitnessmd.MainActivity;
import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.tabs.doctor.Doctor;
import com.master.aluca.fitnessmd.tabs.pedometer.Pedometer;
import com.master.aluca.fitnessmd.tabs.profile.Profile;
import com.master.aluca.fitnessmd.tabs.scale.Scale;
import com.master.aluca.fitnessmd.tabs.settings.SettingsListeners;
import com.master.aluca.fitnessmd.tabs.stats.Statistics;

/**
 * Created by aluca on 11/7/16.
 */
public class TabMenu {

    private static final String LOG_TAG = "Fitness_TabMenu";

    private static TabMenu mInstance = null;
    private Context mContext = null;
    private MainActivity mMainActivity;

    private SettingsListeners mSettingsListeners = null;
    private Statistics mStatistics = null;
    private Pedometer mPedometer = null;
    private Profile mProfile = null;
    private Scale mScale = null;
    private Doctor mDoctor = null;

    /*public static TabMenu getInstance(Context context, MainActivity mainActivity) {
        if (mInstance == null) {
            mInstance = new TabMenu(context, mainActivity);
        }
        return mInstance;
    }*/

    public TabMenu(Context context, MainActivity mainActivity) {
        Log.d(LOG_TAG, "TabMenu");
        mContext = context;
        mMainActivity = mainActivity;

        mStatistics = new Statistics(context, mainActivity);
        mStatistics.setup();

        mSettingsListeners = new SettingsListeners(context, mainActivity);
        mSettingsListeners.setup();

        mPedometer = new Pedometer(context, mainActivity);
        mPedometer.setup();

        mProfile = new Profile(context, mainActivity);
        mProfile.setup();

        mScale = new Scale(context, mainActivity);
        mScale.setup();

        mDoctor = new Doctor(context, mainActivity);
        mDoctor.setup();


    }

    public void setup(byte numberOfTabs) {
        Log.d(LOG_TAG,"setup");
        TabHost host = (TabHost)mMainActivity.findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Pedometer");
        spec.setContent(R.id.tab1);
        //spec.setIndicator("Tab One");
        spec.setIndicator(getTabIndicator(host.getContext(), R.drawable.tab_selector_pedometer)); // new function to inject our own tab layou
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Doctor");
        spec.setContent(R.id.tab2);
        //spec.setIndicator("Tab Two");
        spec.setIndicator(getTabIndicator(host.getContext(), R.drawable.tab_selector_doctor)); // new function to inject our own tab layou
        host.addTab(spec);

        //Tab 3
        spec = host.newTabSpec("Profile");
        spec.setContent(R.id.tab3);
        spec.setIndicator(getTabIndicator(host.getContext(), R.drawable.tab_selector_profile)); // new function to inject our own tab layou
        host.addTab(spec);

        spec = host.newTabSpec("Scale");
        spec.setContent(R.id.tab4);
        spec.setIndicator(getTabIndicator(host.getContext(), R.drawable.tab_selector_scale)); // new function to inject our own tab layou
        host.addTab(spec);

        spec = host.newTabSpec("Stats");
        spec.setContent(R.id.tab5);
        spec.setIndicator(getTabIndicator(host.getContext(), R.drawable.tab_selector_stats)); // new function to inject our own tab layou
        host.addTab(spec);

        spec = host.newTabSpec("Settings");
        spec.setContent(R.id.tabSettings);
        spec.setIndicator(getTabIndicator(host.getContext(), R.drawable.tab_selector_settings)); // new function to inject our own tab layou
        host.addTab(spec);

        host.getTabWidget().setDividerDrawable(null);
        host.getTabWidget().setStripEnabled(false);
        for (int i = 0; i < numberOfTabs; i++) {
            host.getTabWidget().getChildTabViewAt(i).setBackground(null);
        }
        host.getTabWidget().setBackgroundColor(mMainActivity.getResources().getColor(R.color.tab_menu_background));
        host.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Log.d(LOG_TAG,"tabId : " + tabId);
                if (tabId.equalsIgnoreCase("Profile")) {
                    mProfile.syncData();
                }
            }
        });
    }

    private View getTabIndicator(Context context, int icon) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_layout, null);
        ImageView iv = (ImageView) view.findViewById(R.id.imageView);
        iv.setImageResource(icon);
        return view;
    }

    public void setProfilePicture(String tabId, Bitmap image) {
        if(tabId.equalsIgnoreCase("Profile")) {
            mProfile.setProfilePicture(image);
        }
    }

    public int getStepsForCurrentDay() {
        return mPedometer.getStepsForCurrentDay();
    }
}
