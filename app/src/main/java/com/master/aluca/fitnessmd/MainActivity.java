package com.master.aluca.fitnessmd;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.master.aluca.fitnessmd.tabs.TabMenu;
import com.master.aluca.fitnessmd.util.AlertMessage;
import com.master.aluca.fitnessmd.util.ProfilePhotoUtils;
import com.master.aluca.fitnessmd.util.SharedPreferencesManager;
import com.master.aluca.fitnessmd.webserver.WebserverManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by aluca on 11/1/16.
 */
public class MainActivity extends Activity {

    public static final String LOG_TAG = "Fitness_MainActivity";
    private static final byte numberOfTabs = 6;


    private TabMenu mTabMenu = null;

    private FitnessMD_Service mService;
    private ActivityHandler mActivityHandler;

    private ImageView mImageBT = null;
    private TextView mTextStatus = null;

    private boolean alwaysEnableBT;
    private AlertMessage mAlertMessage = new AlertMessage(MainActivity.this);
    private SharedPreferencesManager sharedPreferencesManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(LOG_TAG, "onCreate");
        loadSharedPrefs();

        mActivityHandler = new ActivityHandler();
        sharedPreferencesManager = SharedPreferencesManager.getInstance(getApplicationContext());
        alwaysEnableBT = sharedPreferencesManager.getAlwaysEnableBT();

        // Setup views
        mImageBT = (ImageView) findViewById(R.id.status_title);
        mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
        mTextStatus = (TextView) findViewById(R.id.status_text);
        mTextStatus.setText(getResources().getString(R.string.bt_state_init));

        createAndInitTabs();
        doStartService();
    }

    /**
     * Start service if it's not running
     */
    private void doStartService() {
        Log.d(LOG_TAG, "# Activity - doStartService()");

        if (!FitnessMD_Service.isServiceRunning()) {
            Log.d(LOG_TAG, "service is not running");
            startService(new Intent(this, FitnessMD_Service.class));
        } else {
            Log.d(LOG_TAG, "service is running");
        }
        if (!getApplicationContext().bindService(new Intent(this, FitnessMD_Service.class), mServiceConn, Context.BIND_AUTO_CREATE)) {
            Log.d(LOG_TAG, "unable to bind to service");
        } else {
            Log.d(LOG_TAG, "service binded");
        }

    }

    public FitnessMD_Service getService() {
        return mService;
    }

    /**
     * Service connection
     */
    private ServiceConnection mServiceConn = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            Log.d(LOG_TAG, "Activity - Service connected");
            FitnessMD_Service.FitnessMD_Binder binder = (FitnessMD_Service.FitnessMD_Binder) iBinder;
            mService = binder.getService();

            // Activity couldn't work with mService until connections are made
            // So initialize parameters and settings here, not while running onCreate()
            Log.d(LOG_TAG, "# Activity - initialize()");

            mService.setup(mActivityHandler);
            // If BT is not on, request that it be enabled.
            // RetroWatchService.setupBT() will then be called during onActivityResult
            if (!mService.isBluetoothEnabled() && !alwaysEnableBT) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                LayoutInflater adbInflater = LayoutInflater.from(MainActivity.this);
                View dialogView = adbInflater.inflate(R.layout.layout_enable_bt, null);

                final CheckBox alwaysEnable = (CheckBox) dialogView.findViewById(R.id.alwaysEnableBluetooth);
                builder.setView(dialogView);
                builder.setMessage(Html.fromHtml("FitnessMD wants to turn on Bluetooth."));

                builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        boolean checkBoxResult = false;

                        if (alwaysEnable.isChecked()) {
                            checkBoxResult = true;
                        }
                        sharedPreferencesManager.setAlwaysEnableBT(checkBoxResult);
                        mService.enableBluetooth();
                        return;
                    }
                });

                builder.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Some functions will not work unless you turn on bluetooth", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });

                AlertDialog levelDialog = builder.create();
                levelDialog.show();

                //Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // startActivityForResult(enableIntent, Constants.REQUEST_ENABLE_BT);
            } else if (alwaysEnableBT) {
                mService.enableBluetooth();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };


    /**
     * **************************************************
     * Handler, Callback, Sub-classes
     * ****************************************************
     */

    public class ActivityHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // BT state messages
                case Constants.MESSAGE_BT_STATE_INITIALIZED:
                    mTextStatus.setText(getResources().getString(R.string.bt_title) + ": " +
                            getResources().getString(R.string.bt_state_init));
                    mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_invisible));
                    break;
                case Constants.MESSAGE_BT_STATE_CONNECTING:
                    mTextStatus.setText(getResources().getString(R.string.bt_title) + ": " +
                            getResources().getString(R.string.bt_state_connect));
                    mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_away));
                    break;
                case Constants.MESSAGE_BT_STATE_CONNECTED:
                    /*if(mService != null) {
                        String deviceName = mService.getDeviceName();
                        if(deviceName != null) {
                            mTextStatus.setText(getResources().getString(R.string.bt_title) + ": " +
                                    getResources().getString(R.string.bt_state_connected) + " " + deviceName);
                            mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_online));
                        }
                    }*/
                    break;
                case Constants.MESSAGE_BT_STATE_ERROR:
                    mTextStatus.setText(getResources().getString(R.string.bt_state_error));
                    mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
                    break;

                // BT Command status
                /*case Constants.MESSAGE_CMD_ERROR_NOT_CONNECTED:
                    mTextStatus.setText(getResources().getString(R.string.bt_cmd_sending_error));
                    mImageBT.setImageDrawable(getResources().getDrawable(android.R.drawable.presence_busy));
                    break;

                ////////////////////////////////////////////
                // Contents changed
                ////////////////////////////////////////////
                case Constants.MESSAGE_READ_ACCEL_REPORT:
                    ActivityReport ar = (ActivityReport)msg.obj;
                    if(ar != null) {
                        TimelineFragment frg = (TimelineFragment) mSectionsPagerAdapter.getItem(LLFragmentAdapter.FRAGMENT_POS_TIMELINE);
                        frg.showActivityReport(ar);
                    }
                    break;

                case Constants.MESSAGE_READ_ACCEL_DATA:
                    ContentObject co = (ContentObject)msg.obj;
                    if(co != null) {
                        GraphFragment frg = (GraphFragment) mSectionsPagerAdapter.getItem(LLFragmentAdapter.FRAGMENT_POS_GRAPH);
                        frg.drawAccelData(co.mAccelData);
                    }
                    break;*/
                case Constants.MESSAGE_FIRST_ACCEL_READ:


                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }    // End of class ActivityHandler

    /*****************************************************
     *	Public classes
     ******************************************************/

    /**
     * Receives result from external activity
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult " + resultCode);
        if (resultCode != 0) {
            switch (requestCode) {
                case Constants.REQUEST_ENABLE_BT:
                    // When the request to enable Bluetooth returns
                    if (resultCode == Activity.RESULT_OK) {
                        // Bluetooth is now enabled, so set up a BT session
                        mService.initializeBluetoothManager();
                    } else {
                        // User did not enable Bluetooth or an error occured
                        Log.e(LOG_TAG, "BT is not enabled");
                        Toast.makeText(this, "Bluetooth was not enabled by user", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.TAKE_PHOTO:
                    Log.d(LOG_TAG, "TAKE_PHOTO");
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    String url = ProfilePhotoUtils.insertImage(getContentResolver(), photo, "profile_pic", "desc");
                    Uri uri = Uri.parse(url);
                    photo = ProfilePhotoUtils.rotatePhoto(getContentResolver(), uri);
                    mTabMenu.setProfilePicture("Profile", photo);
                    sharedPreferencesManager.setProfilePictureURI(uri.toString());
                    break;
                case Constants.GET_GALLERY_IMAGE:
                    Log.d(LOG_TAG, "GET_GALLERY_IMAGE");
                    //Log.d(LOG_TAG, "data extras : " + data.getExtras().get("data"));
                    Uri mImageUri = data.getData();
                    Log.d(LOG_TAG, "Uri : " + data.getData());
                    Bitmap image = ProfilePhotoUtils.getProfilePicFromGallery(getContentResolver(), mImageUri);
                    mTabMenu.setProfilePicture("Profile",image);
                    sharedPreferencesManager.setProfilePictureURI(mImageUri.toString());

            }    // End of switch(requestCode)
        } else Log.d(LOG_TAG,"resultCode is 0 ");

    }




    private void createAndInitTabs() {
        mTabMenu = new TabMenu(getApplicationContext(), MainActivity.this);
        mTabMenu.setup(numberOfTabs);
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");

        sharedPreferencesManager.setStepsForCurrentDay(mTabMenu.getStepsForCurrentDay(), false);

        WebserverManager mWebserverManager = WebserverManager.getInstance(this);
        mWebserverManager.destroyMeteor();

        super.onDestroy();
        Log.d(LOG_TAG, "# Activity - finalizeActivity()");


        /*if(!AppSettings.getBgService()) {
            mService.finalizeService();
            stopService(new Intent(this, FitnessMD_Service.class));
        } else {

        }*/
        // Clean used resources
        //mService.finalizeService();
        //stopService(new Intent(this, FitnessMD_Service.class));
    }

    public void loadSharedPrefs() {

        // Define default return values. These should not display, but are needed
        final String STRING_ERROR = "error!";
        final Integer INT_ERROR = -1;
        // ...
        final Set<String> SET_ERROR = new HashSet<>(1);

        // Add an item to the set
        SET_ERROR.add("Set Error!");

        // Loop through the Shared Prefs
        Log.i(LOG_TAG, "-----------------------------------");
        Log.i(LOG_TAG, "-------------------------------------");

        //for (String pref_name: prefs) {

        SharedPreferences preference = getSharedPreferences(Constants.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        Map<String, ?> prefMap = preference.getAll();

        Object prefObj;
        Object prefValue = null;

        for (String key : prefMap.keySet()) {

            prefObj = prefMap.get(key);

            if (prefObj instanceof String) prefValue = preference.getString(key, STRING_ERROR);
            if (prefObj instanceof Integer) prefValue = preference.getInt(key, INT_ERROR);
            // ...
            if (prefObj instanceof Set) prefValue = preference.getStringSet(key, SET_ERROR);

            Log.i(LOG_TAG,String.format("Shared Preference : %s - %s - %s", Constants.SHARED_PREFERENCES, key, String.valueOf(prefValue)));

        }

        Log.i(LOG_TAG, "-------------------------------------");

        //}

        Log.i(LOG_TAG, "------------------------------------");

    }


}